import sbt.Keys._
import sbt.Project.projectToRef
// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import org.irundaia.sbt.sass._

// a special crossProject for configuring a JS/JVM/shared structure
lazy val shared = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("shared"))
  .settings(
      scalaVersion := Settings.v.scala,
      libraryDependencies ++= Settings.sharedDependencies.value
  )
  // set up settings specific to the JS project
  .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJVM = shared.jvm.settings(name := "sharedJVM")

lazy val sharedJS = shared.js.settings(name := "sharedJS")

// use eliding to drop some debug code in the production build
lazy val elideOptions = settingKey[Seq[String]]("Set limit for elidable functions")

// instantiate the JS project for SBT with some additional settings
lazy val client: Project = (project in file("client"))
  .settings(
      name := "client",
      version := Settings.v.app,
      scalaVersion := Settings.v.scala,
      scalacOptions ++= Settings.scalacOptions,
      libraryDependencies ++= Settings.scalajsDependencies.value,
      // by default we do development build, no eliding
      elideOptions := Seq(),
      scalacOptions ++= elideOptions.value,
      jsDependencies ++= Settings.jsDependencies.value,
      // RuntimeDOM is needed for tests
      jsEnv in Test := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
      // yes, we want to package JS dependencies
      skip in packageJSDependencies := false,
      // use Scala.js provided launcher code to start the client app
      scalaJSUseMainModuleInitializer := true,
      scalaJSUseMainModuleInitializer in Test := false,
      // use uTest framework for tests
      testFrameworks += new TestFramework("utest.runner.Framework"),
      dependencyOverrides ++= Settings.dependencyOverrides.value
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(sharedJS)

// Client projects (just one in this case)
lazy val clients = Seq(client)

// instantiate the JVM project for SBT with some additional settings
lazy val server = (project in file("server"))
  .settings(
      name := "server",
      version := Settings.v.app,
      scalaVersion := Settings.v.scala,
      scalacOptions ++= Settings.scalacOptions,
      libraryDependencies ++= Settings.jvmDependencies.value, 
      libraryDependencies += guice,
      commands += ReleaseCmd,
      // triggers scalaJSPipeline when using compile or continuous compilation
      compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
      // connect to the client project
      scalaJSProjects := clients,
      pipelineStages in Assets := Seq(scalaJSPipeline),
      pipelineStages := Seq(digest, gzip),
      // compress CSS
      SassKeys.cssStyle in Assets := Minified,
      dependencyOverrides ++= Settings.dependencyOverrides.value
  )
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin) // use the standard directory layout instead of Play's custom
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJVM)

// Command for building a release
lazy val ReleaseCmd = Command.command("release") {
    state => "set elideOptions in client := Seq(\"-Xelide-below\", \"WARNING\")" ::
      "client/clean" ::
      "client/test" ::
      "server/clean" ::
      "server/test" ::
      "server/dist" ::
      "set elideOptions in client := Seq()" ::
      state
}

// lazy val root = (project in file(".")).aggregate(client, server)

// loads the Play server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

# SBT build definition

Since Scala.js is quite new and it's been evolving even rather recently, building Scala.js applications with SBT is not as clear as it could
be. Yes, the documentation and tutorials give you the basics, but what if you want something more, like configure a custom directory layout?

The `build.sbt` in this tutorial shows you some typical cases you might run into in your own application. The basic structure of the `build.sbt`
is built on top of the [example](https://github.com/vmunier/play-with-scalajs-example/blob/master/build.sbt) provided by Vincent Munier, author of
the [sbt-play-scalajs](https://github.com/vmunier/sbt-play-scalajs) plugin.

The build defines three separate projects:
* shared
* client
* server

## Shared project

First one is a special Scala.js `CrossProject` that actually contains two projects: one for JS and one for JVM. This `shared` project contains classes, libraries
and resources shared between the client and server. In the context of this tutorial it means just the `Api.scala` trait and `TodoItem.scala` case class.
In a more realistic application you would have your data models etc. defined here.

```scala
lazy val shared = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("shared"))
  .settings(
      scalaVersion := Settings.v.scala,
      libraryDependencies ++= Settings.sharedDependencies.value
  )
  // set up settings specific to the JS project
  .jsConfigure(_ enablePlugins ScalaJSWeb)
```

The shared dependencies include libraries used by both client and server such as `autowire` and `boopickle` for client/server communication.
```scala
val sharedDependencies = Def.setting(Seq(
  "com.lihaoyi" %%% "autowire" % v.autowire,
  "io.suzaku" %%% "boopickle" % v.booPickle
))
```

## Client project

Client is defined as a normal Scala.js project by enabling the `ScalaJSPlugin` on it.

```scala
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
```

First few settings are normal Scala settings, but let's go through the remaining settings to explain what they do.

```scala
  // use eliding to drop some debug code in the production build
  lazy val elideOptions = settingKey[Seq[String]]("Set limit for elidable functions")
```
Eliding is used to remove code that is not needed in the production build, such as debug logging. This setting is empty by default, but is enabled in
the `release` command.

```scala
    jsDependencies ++= Settings.jsDependencies.value,
    // RuntimeDOM is needed for tests
    jsEnv in Test := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    // yes, we want to package JS dependencies
    skip in packageJSDependencies := false,
```
The `jsDependencies` defines a set of JavaScript libraries your application depends on. These are also packaged into a single `.js` file for easy
consumptions. For `test` phase we include the `RuntimeDOM` so that Scala.js plugin knows to use PhantomJS instead of the default Rhino to run the tests.
Make sure you have installed [PhantomJS](http://phantomjs.org/) before running the tests.

```scala
    // use Scala.js provided launcher code to start the client app
    scalaJSUseMainModuleInitializer := true,
    scalaJSUseMainModuleInitializer in Test := false,
```
This setting informs Scala.js plugin to generate a special `launcher.js` file, which is loaded last and invokes your `main` method. Using a launcher keeps
your HTML template clean, as you don't need to specify the `main` function there.

```scala
    // use uTest framework for tests
    testFrameworks += new TestFramework("utest.runner.Framework")
```
Lets SBT know that we are using uTest framework for tests.

```scala
    dependencyOverrides ++= Settings.dependencyOverrides.value
```
Fixes unresolved deps issue: https://github.com/webjars/webjars/issues/1789

```scala
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .dependsOn(sharedJS)
```
We enable both Scala.js and Scala.js-for-Play plugins. Finally the `client` project needs to depend on the `shared` project to get access to shared code
and resources.

## Server project

The server project is a normal Play project with a few twists to make client integration a breeze. Most of the heavy-lifting is done by the `ScalaJSPlay`
plugin, which is automatically included to all projects using `PlayScala` plugin.

```scala
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
```
As with the client project, the first few settings are just normal SBT settings, so let's focus on the more interesting ones.

```scala
    commands += ReleaseCmd,
```
We define a new SBT command `release` to run a sequence of commands to produce a [distribution package](production-build.md).

```scala
    // connect to the client project
    scalaJSProjects := clients,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
```
Let the plugin know where our client project is and enable Scala.js processing in the pipeline.

```scala
    // compress CSS
    SassKeys.cssStyle in Assets := Minified,
```
This instructs the `sbt-sassify` plugin to minify the produced CSS.

```scala
    dependencyOverrides ++= Settings.dependencyOverrides.value
```
Fixes unresolved deps issue: https://github.com/webjars/webjars/issues/1789


```scala
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin) // use the standard directory layout instead of Play's custom
```
We use Play, but not its default layout. Instead we prefer the normal SBT layout with `src/main/scala` structure.

```scala
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJVM)
```
Server aggregates the client and also depends on the `shared` project to get access to shared code and resources.

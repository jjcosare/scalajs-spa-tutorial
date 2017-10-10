// repository for Typesafe plugins
resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.28")

addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.9-0.6")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "0.6.0")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "0.6.0")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.3.7")

addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.13")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.22")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

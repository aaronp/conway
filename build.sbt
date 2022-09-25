enablePlugins(ScalaJSPlugin)
name := "life"
scalaVersion := "3.2.0"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0"
libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.11.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.13" % "test"
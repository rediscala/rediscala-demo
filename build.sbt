name := "rediscala-demo"

scalacOptions += "-deprecation"

run / fork := true

scalaVersion := "3.7.4"

libraryDependencies ++= Seq(
  "io.github.rediscala" %% "rediscala" % "2.0.1"
)

TaskKey[Unit]("runAllMain") := {
  val r = (run / runner).value
  val classpath = (Compile / fullClasspath).value
  val log = streams.value.log
  (Compile / discoveredMainClasses).value.sorted.foreach(c => r.run(c, classpath.map(_.data), Nil, log))
}

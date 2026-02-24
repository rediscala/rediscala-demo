name := "rediscala-demo"

scalacOptions += "-deprecation"

run / fork := true

scalaVersion := "3.8.2"

libraryDependencies ++= Seq(
  "io.github.rediscala" %% "rediscala" % "2.0.2"
)

TaskKey[Unit]("runAllMain") := Def.uncached {
  val r = (run / runner).value
  val converter = fileConverter.value
  val classpath = (Compile / fullClasspath).value.map(_.data).map(converter.toPath)
  val log = streams.value.log
  (Compile / discoveredMainClasses).value.sorted.foreach(c => r.run(c, classpath, Nil, log))
}

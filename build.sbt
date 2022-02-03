name := "rediscala-demo"

scalacOptions += "-deprecation"

run / fork := true

def Scala212 = "2.12.15"
def Scala213 = "2.13.8"
def Scala3 = "3.1.1"

scalaVersion := Scala213

crossScalaVersions := Seq(Scala212, Scala213, Scala3)

libraryDependencies ++= Seq(
  "io.github.rediscala" %% "rediscala" % "1.10.0"
)

TaskKey[Unit]("runAllMain") := {
  val r = (run / runner).value
  val classpath = (Compile / fullClasspath).value
  val log = streams.value.log
  (Compile / discoveredMainClasses).value.sorted.foreach(c => r.run(c, classpath.map(_.data), Nil, log))
}

name := "rediscala-demo"

scalacOptions += "-deprecation"

scalacOptions ++= {
  scalaBinaryVersion.value match {
    case "3" =>
      Nil
    case _ =>
      Seq(
        "-Xsource:3",
      )
  }
}

run / fork := true

def Scala213 = "2.13.16"
def Scala3 = "3.7.0"

scalaVersion := Scala213

crossScalaVersions := Seq(Scala213, Scala3)

libraryDependencies ++= Seq(
  "io.github.rediscala" %% "rediscala" % "1.17.0"
)

TaskKey[Unit]("runAllMain") := {
  val r = (run / runner).value
  val classpath = (Compile / fullClasspath).value
  val log = streams.value.log
  (Compile / discoveredMainClasses).value.sorted.foreach(c => r.run(c, classpath.map(_.data), Nil, log))
}

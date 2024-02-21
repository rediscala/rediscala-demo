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

def Scala212 = "2.12.19"
def Scala213 = "2.13.12"
def Scala3 = "3.4.0"

scalaVersion := Scala213

crossScalaVersions := Seq(Scala212, Scala213, Scala3)

libraryDependencies ++= Seq(
  "io.github.rediscala" %% "rediscala" % "1.14.0-akka"
)

TaskKey[Unit]("runAllMain") := {
  val r = (run / runner).value
  val classpath = (Compile / fullClasspath).value
  val log = streams.value.log
  (Compile / discoveredMainClasses).value.sorted.foreach(c => r.run(c, classpath.map(_.data), Nil, log))
}

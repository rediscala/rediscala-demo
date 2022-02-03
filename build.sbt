name := "rediscala-demo"

scalacOptions += "-deprecation"

def Scala212 = "2.12.15"
def Scala213 = "2.13.8"
def Scala3 = "3.1.1"

scalaVersion := Scala213

crossScalaVersions := Seq(Scala212, Scala213, Scala3)

addCommandAlias("SetScala2_12", s"++ ${Scala212}! -v")
addCommandAlias("SetScala2_13", s"++ ${Scala213}! -v")
addCommandAlias("SetScala3", s"++ ${Scala3}! -v")

libraryDependencies ++= Seq(
  "io.github.rediscala" %% "rediscala" % "1.10.0"
)

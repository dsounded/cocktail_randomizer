name := "cocktail_randomizer"

version := "1.0"

lazy val `cocktail_randomizer` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.2.0",
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "com.typesafe.akka" %% "akka-actor" % "2.4.12",
  specs2 % Test)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

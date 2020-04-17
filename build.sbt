name := "scala_blog_api"
 
version := "1.0" 
      
lazy val `scala_blog_api` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
scalaVersion := "2.13.1"

libraryDependencies ++= Seq(ehcache , ws , specs2 % Test , guice )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "8.0.13",
  "com.typesafe.play" %% "play-slick" % "4.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.2",
  "com.github.t3hnar" %% "scala-bcrypt" % "4.1",
"com.pauldijou" %% "jwt-play-json" % "4.2.0"
)

      
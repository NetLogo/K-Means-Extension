scalaVersion := "2.9.2"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
                      "-encoding", "us-ascii")

libraryDependencies ++= Seq(
  "org.nlogo" % "NetLogoLite" % "5.0.4" from
    "http://ccl.northwestern.edu/netlogo/5.0.4/NetLogoLite.jar",
  "net.sf.jung" % "jung-algorithms" % "2.0.1"
)

name := "k-means"

NetLogoExtension.settings

NetLogoExtension.classManager := "org.nlogo.extensions.kmeans.KMeansExtension"

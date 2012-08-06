name := "elasticsearch.scorer"

version := "0.0.3-snapshot"

organization := "com.foursquare"

crossScalaVersions := Seq("2.9.1")

libraryDependencies <++= (scalaVersion) { scalaVersion =>
  val specsVersion = scalaVersion match {
    case "2.8.0" => "1.6.5"
    case "2.9.1" => "1.6.9"
    case _       => "1.6.8"
  }
  val scalaCheckVersion = scalaVersion match {
    case "2.8.0" => "1.8"
    case "2.8.1" => "1.8"
    case _ => "1.9"
  }
  Seq(
    "junit"                    % "junit"               % "[4.8.2,)"        % "test",
    "com.novocode"             % "junit-interface"     % "[0.7,)"        % "test" ,
    "org.scala-tools.testing" %% "specs"               % specsVersion % "test",
    "org.elasticsearch"        % "elasticsearch"  % "0.19.2",
    "org.scala-tools.testing" %% "scalacheck"         % scalaCheckVersion   % "test",
    "org.scalaj"              %% "scalaj-collection" % "1.2",
    "org.testng"              % "testng" % "6.3.1" % "test",
    "org.hamcrest"            % "hamcrest-core" % "1.3.RC2" % "test",
    "org.hamcrest"            % "hamcrest-library" % "1.3.RC2" % "test"
  )
}

publishTo <<= (version) { v =>
  val nexus = "https://oss.sonatype.org/"
  if (v.endsWith("-SNAPSHOT"))
    Some("snapshots" at nexus+"content/repositories/snapshots/")
  else
    Some("releases" at nexus+"service/local/staging/deploy/maven2")
}

resolvers += "Bryan J Swift Repository" at "http://repos.bryanjswift.com/maven2/"

resolvers += "twitter maven repo" at "http://maven.twttr.com/"

resolvers += "codehaus maven repo" at "http://repository.codehaus.org/"

resolvers += "sonatype maven repo" at "http://oss.sonatype.org/content/repositories/releases/"

resolvers <++= (version) { v =>
  if (v.endsWith("-SNAPSHOT"))
    Seq(ScalaToolsSnapshots)
  else
    Seq()
}

scalacOptions ++= Seq("-deprecation", "-unchecked")

testFrameworks += new TestFramework("com.novocode.junit.JUnitFrameworkNoMarker")


credentials ++= {
  val sonaType = ("Sonatype Nexus Repository Manager", "oss.sonatype.org")
  def loadMavenCredentials(file: java.io.File) : Seq[Credentials] = {
    xml.XML.loadFile(file) \ "servers" \ "server" map (s => {
      val host = (s \ "id").text
      val realm = if (host == sonaType._2) sonaType._1 else "Unknown"
      Credentials(realm, host, (s \ "username").text, (s \ "password").text)
    })
  }
  val ivyCredentials   = Path.userHome / ".ivy2" / ".credentials"
  val mavenCredentials = Path.userHome / ".m2"   / "settings.xml"
  (ivyCredentials.asFile, mavenCredentials.asFile) match {
    case (ivy, _) if ivy.canRead => Credentials(ivy) :: Nil
    case (_, mvn) if mvn.canRead => loadMavenCredentials(mvn)
    case _ => Nil
  }
}

publishMavenStyle := true

pomIncludeRepository := { x => false }

pomExtra := (
<url>https://github.com/foursquare/es-scorer-plugin</url>
<licenses>
  <license>
    <name>Apache 2</name>
    <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    <distribution>repo</distribution>
    <comments>A business-friendly OSS license</comments>
  </license>
</licenses>
<scm>
 <url>git@github.com/foursquare/es-scorer-plugin.git</url>
 <connection>scm:git:git@github.com/foursquare/es-scorer-plugin.git</connection>
</scm>
<developers>
 <developer>
 <id>holdenkarau></id>
 <name>Holden Karau</name>
 <email>holden@foursquare.com</email>
 </developer>
</developers>
)
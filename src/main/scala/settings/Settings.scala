package settings

import java.io.{BufferedReader, FileNotFoundException, InputStreamReader}
import java.util.Properties

import scala.collection.JavaConverters._

object PredictorSettings {
  lazy val modelDirectory = Settings.get("model.directory").getOrElse("")
  lazy val modelTrainInterval = Settings.get("model.train.interval.days").map(p => p.toInt).getOrElse(90)
  lazy val repositoryOwner = Settings.get("repository.owner").getOrElse("")
  lazy val repositoryName = Settings.get("repository.name").getOrElse("")
  lazy val pullRequestLimit = Settings.get("pullrequest.limit").map(p => p.toInt).getOrElse(1000)
  lazy val windowInterval = Settings.get("window.interval.hours").map(p => p.toInt).getOrElse(24)
}

object RSettings {
  lazy val rscriptLocation = Settings.get("rscript.location").getOrElse("")
  lazy val scriptDirectory = Settings.get("script.directory").getOrElse("")
  lazy val probabilityThreshold = Settings.get("probability.threshold").map(p => p.toDouble).getOrElse(0.5)
  lazy val resultLimit = Settings.get("result.limit").map(p => p.toInt).getOrElse(5)
}

object GHTorrentSettings {
  lazy val host = Settings.get("ghtorrent.host").getOrElse("localhost")
  lazy val port = Settings.get("ghtorrent.port").map(p => p.toInt).getOrElse(3306)
  lazy val username = Settings.get("ghtorrent.username").getOrElse("")
  lazy val password = Settings.get("ghtorrent.password").getOrElse("")
  lazy val database = Settings.get("ghtorrent.database").getOrElse("")
}

object MongoDbSettings {
  lazy val host = Settings.get("mongodb.host").getOrElse("localhost")
  lazy val port = Settings.get("mongodb.port").map(p => p.toInt).getOrElse(27017)
  lazy val username = Settings.get("mongodb.username").getOrElse("")
  lazy val password = Settings.get("mongodb.password").getOrElse("")
  lazy val database = Settings.get("mongodb.database").getOrElse("")
  lazy val collectionCommits = Settings.get("mongodb.collection.commits").getOrElse("")
  lazy val collectionPullRequests = Settings.get("mongodb.collection.pullrequests").getOrElse("")
}

object Settings {
  val fileName = "settings.properties"
  val resource = getClass.getResourceAsStream("/" + fileName)
  val data = read

  /**
   * @param property The name of the property.
   * @return True iff there exists a property with the give name.
   */
  def has(property: String): Boolean =
    data.get(property).isDefined

  /**
   * @param property The name of the property.
   * @return The value of the property.
   */
  def get(property: String): Option[String] =
    data.get(property)

  /**
   * Read the properties from the config file.
   * @return A map with the properties.
   */
  private def read: Map[String, String] = {
    if (resource == null)
      throw new FileNotFoundException(
        s"The configuration file was not found. Please make sure you copied $fileName.dist to $fileName.")

    // Read properties file
    val reader = new BufferedReader(new InputStreamReader(resource, java.nio.charset.StandardCharsets.UTF_8))
    val props = new Properties
    props.load(reader)
    props.readSystemOverride()
    props.asScala.toMap
  }

  implicit class RichProperties(properties: Properties) {
    def readSystemOverride(): Unit = {
      val keys = properties.keySet().asScala.collect({ case str: String => str })

      keys.foreach(key => {
        val propOverride = System.getProperty(key)
        if (propOverride != null)
          properties.put(key, propOverride)
      })
    }
  }
}

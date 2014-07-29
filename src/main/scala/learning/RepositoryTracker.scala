package learning

import java.sql.Timestamp

import ghtorrent.MongoDatabase
import ghtorrent.Schema.Tables
import git.PullRequest
import org.joda.time.DateTime
import settings.{PredictorSettings, MongoDbSettings, GHTorrentSettings}
import scala.slick.driver.MySQLDriver.simple._

class RepositoryTracker(owner: String, repository: String) {
  private val dbUrl = s"jdbc:mysql://${GHTorrentSettings.host}:${GHTorrentSettings.port}/${GHTorrentSettings.database}"
  private val dbDriver = "com.mysql.jdbc.Driver"

  lazy val ghRepoId = getRepoId
  lazy val commits = getCommits
  lazy val authors = new AuthorTrackers(this)
  lazy val pullRequests = getPullRequests

  implicit lazy val session = Database.forURL(dbUrl, GHTorrentSettings.username, GHTorrentSettings.password, driver = dbDriver).createSession()

  def getSnapshots = {
//    println(commits.length)
//    println(commits.head)
    println(pullRequests.length)
    null
  }

  private def getRepoId = {
    val projectIds = for {
      // From
      p <- Tables.projects
      u <- Tables.users
      // Join
      if p.ownerId === u.id
      // Where
      if u.login === owner
      if p.name === repository
    } yield p.id

    projectIds.first
  }

  private def getCommits = {
    val projectCommits = for {
      // From
      c <- Tables.commits
      // Where
      if c.projectId === ghRepoId
    } yield c

    projectCommits.list
  }

  private def getPullRequests = {
    val extIds = for {
      // From
      p <- Tables.pullRequests.sortBy(_.number.desc)
      h <- Tables.pullRequestHistory
      // Join
      if p.id === h.pullRequestId
      // Where
      if p.baseRepoId === ghRepoId
      if h.action === "closed"
      if h.extRefId =!= ""
    } yield (h.extRefId, h.createdAt)

    val ids = extIds.take(PredictorSettings.pullRequestLimit).list

    val mongo = new MongoDatabase(MongoDbSettings.host,
      MongoDbSettings.port,
      MongoDbSettings.username,
      MongoDbSettings.password,
      MongoDbSettings.database,
      MongoDbSettings.collection)

    val fields = List(
      "number",
      "user.login",
      "title",
      "base.ref",
      "created_at",
      "merged_at",
      "closed_at")

    // Get PR info from MongoDB
    mongo.open()
    val objects = ids.map(id => (mongo.getObject(id._1, fields), id._2))
    mongo.close()

    // Map to actual PR objects
    val pullRequests = objects.map { pair =>
      val obj = pair._1
      PullRequest(
        obj.get("number").get.toInt,
        obj.get("user.login").get,
        obj.get("base.ref").get,
        obj.get("title").get,
        obj.get("created_at").map(s => new DateTime(s)).get,
        pair._2
      )
    }

    pullRequests
  }
}

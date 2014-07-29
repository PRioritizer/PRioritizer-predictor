package learning

import ghtorrent.MongoDatabase
import ghtorrent.Schema.Tables
import git.{Commit, PullRequest}
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

  implicit lazy val mongo = new MongoDatabase(MongoDbSettings.host,
    MongoDbSettings.port,
    MongoDbSettings.username,
    MongoDbSettings.password,
    MongoDbSettings.database).open()
  implicit lazy val session = Database.forURL(dbUrl, GHTorrentSettings.username, GHTorrentSettings.password, driver = dbDriver).createSession()

  def getSnapshots: Iterable[(PullRequest, Important)] = {
    val trackers = pullRequests.map(pr => new PullRequestTracker(this, pr))
    val snapshots = trackers.flatMap(t => t.track)

    snapshots
  }

  private def getRepoId: Int = {
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

  private def getCommits: List[Commit] = {
    val projectCommits = for {
      // From
      pc <- Tables.projectCommits
      c <- Tables.commits
      // Join
      if c.id === pc.commitId
      // Where
      if pc.projectId === ghRepoId
    } yield c

    projectCommits.list
  }

  private def getPullRequests: List[PullRequest] = {
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

    // TODO: group by ext id
    val ids = extIds.list.distinct.take(PredictorSettings.pullRequestLimit)

    // Get PR info from MongoDB
    val pullRequests = ids.map((getFromMongo _).tupled)

    pullRequests
  }

  private def getFromMongo(id: String, closedAt: DateTime): PullRequest = {
    val fields = List(
      "number",
      "user.login",
      "base.ref",
      "title",
      "created_at")

    val obj = mongo.getById(MongoDbSettings.collectionPullRequests, id, fields)

    PullRequest(
      obj.get("number").get.asInstanceOf[Int],
      obj.get("user.login").get.asInstanceOf[String],
      obj.get("base.ref").get.asInstanceOf[String],
      obj.get("title").get.asInstanceOf[String],
      obj.get("created_at").map(s => new DateTime(s)).get,
      closedAt
    )
  }
}

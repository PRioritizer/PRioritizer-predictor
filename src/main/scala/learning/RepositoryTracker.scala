package learning

import ghtorrent.Schema.Tables
import ghtorrent.{DateTimeMapper, MongoDatabase}
import git.{Commit, PullRequest}
import org.joda.time.DateTime
import settings.{GHTorrentSettings, MongoDbSettings, PredictorSettings}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.slick.driver.MySQLDriver.simple._

class RepositoryTracker(owner: String, repository: String) {
  private val dbUrl = s"jdbc:mysql://${GHTorrentSettings.host}:${GHTorrentSettings.port}/${GHTorrentSettings.database}"
  private val dbDriver = "com.mysql.jdbc.Driver"

  lazy val authors = new AuthorTrackers(this)

  // Manual implement laziness (because lazy freezes on Await?)
  private var _ghRepoId: Int = -1
  def ghRepoId = {
    if (_ghRepoId == -1)
      _ghRepoId = Await.result(getRepoId, Duration.Inf)
    _ghRepoId
  }

  private var _pullRequests: List[PullRequest] = _
  def pullRequests = {
    if (_pullRequests == null)
      _pullRequests = Await.result(getPullRequests, Duration.Inf)
    _pullRequests
  }

  private var _commits: List[Commit] = null
  def commits = {
    if (_commits == null)
      _commits = Await.result(getCommits, Duration.Inf)
    _commits
  }

  implicit lazy val mongo = new MongoDatabase(MongoDbSettings.host,
    MongoDbSettings.port,
    MongoDbSettings.username,
    MongoDbSettings.password,
    MongoDbSettings.database).open()
  implicit lazy val session = Database.forURL(dbUrl, GHTorrentSettings.username, GHTorrentSettings.password, driver = dbDriver).createSession()

  def getSnapshots: Future[List[(PullRequest, Important)]] = Future {
    if (ghRepoId == 0)
      throw new Exception(s"Repository $owner/$repository not found in GHTorrrent")

    val trackers = pullRequests.map(pr => new PullRequestTracker(this, pr))
    val fSnapshots = Future.sequence(trackers.map(t => t.track)).map(l => l.flatten)
    val snapshots = Await.result(fSnapshots, Duration.Inf)
    snapshots.toList
  }

  private def getRepoId: Future[Int] = Future {
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

    projectIds.firstOption.getOrElse(0)
  }

  private def getCommits: Future[List[Commit]] = Future {
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

  private def getPullRequests: Future[List[PullRequest]] = Future {
    val extIds = for {
    // From
      p <- Tables.pullRequests
      h <- Tables.pullRequestHistory
      // Join
      if p.id === h.pullRequestId
      // Where
      if p.baseRepoId === ghRepoId
      if h.action === "closed"
      if h.extRefId =!= ""
    } yield (h.extRefId, h.createdAtAsTimestamp)

    // Group by extId, get max closedAt
    val distinct = extIds
      .groupBy { _._1 }
      .map { case (id, g) => (id, g.map(_._2).max) }
      .sortBy(_._2.desc)

    // Execute query and map results
    val list = distinct.list.map(row => (row._1, DateTimeMapper.convert(row._2.get)))
    val ids = list.take(PredictorSettings.pullRequestLimit)

    // Get PR info from MongoDB
    val pullRequests = ids.map((getFromMongo _).tupled)

    Await.result(Future.sequence(pullRequests), Duration.Inf)
  }

  private def getFromMongo(id: String, closedAt: DateTime): Future[PullRequest] = Future {
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

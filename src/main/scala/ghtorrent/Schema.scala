package ghtorrent

import ghtorrent.DateTimeMapper._
import git._
import org.joda.time.DateTime

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.lifted.ProvenShape._

object Schema {
  object Tables {
    val projects = TableQuery[Projects]
    val pullRequests = TableQuery[PullRequests]
    val users = TableQuery[Users]
    val commits = TableQuery[Commits]
    val issues = TableQuery[Issues]
    val pullRequestHistory = TableQuery[PullRequestHistory]
    val pullRequestCommits = TableQuery[PullRequestCommits]
    val projectCommits = TableQuery[ProjectCommits]
    val projectMembers = TableQuery[ProjectMembers]
    val issueEvents = TableQuery[IssueEvents]
    val comments = TableQuery[Comments]
    val reviewComments = TableQuery[ReviewComments]
  }

  object TableNames {
    val projects = "projects"
    val pullRequests = "pull_requests"
    val users = "users"
    val commits = "commits"
    val issues = "issues"
    val pullRequestHistory = "pull_request_history"
    val pullRequestCommits = "pull_request_commits"
    val projectCommits = "project_commits"
    val projectMembers = "project_members"
    val issueEvents = "issue_events"
    val comments = "issue_comments"
    val reviewComments = "pull_request_comments"
  }

  class Projects(tag: Tag) extends Table[(Int, Int, String)](tag, TableNames.projects) {
    def id = column[Int]("id", O.PrimaryKey)
    def name = column[String]("name")
    def ownerId = column[Int]("owner_id")

    def * = (id, ownerId, name)
  }

  class PullRequests(tag: Tag) extends Table[(Int, Int, Int, Int)](tag, TableNames.pullRequests) {
    def id = column[Int]("id", O.PrimaryKey)
    def number = column[Int]("pullreq_id")
    def userId = column[Int]("user_id")
    def baseRepoId = column[Int]("base_repo_id")

    def * = (id, number, userId, baseRepoId)
  }

  class Users(tag: Tag) extends Table[(Int, String)](tag, TableNames.users) {
    def id = column[Int]("id", O.PrimaryKey)
    def login = column[String]("login")

    def * = (id, login)
  }

  class Commits(tag: Tag) extends Table[Commit](tag, TableNames.commits) {
    def id = column[Int]("id", O.PrimaryKey)
    def projectId = column[Int]("project_id")
    def authorId = column[Int]("author_id")
    def createdAt = column[DateTime]("created_at")
    def sha = column[String]("sha")

    def * = (createdAt, sha) <> (Commit.tupled, Commit.unapply)
  }

  class Issues(tag: Tag) extends Table[(Int, Int)](tag, TableNames.issues) {
    def id = column[Int]("id", O.PrimaryKey)
    def pullRequestId = column[Int]("pull_request_id")

    def * = (id, pullRequestId)
  }

  class PullRequestHistory(tag: Tag) extends Table[Event](tag, TableNames.pullRequestHistory) {
    def pullRequestId = column[Int]("pull_request_id")
    def userId = column[Int]("actor_id")
    def action = column[String]("action")
    def createdAt = column[DateTime]("created_at")
    def createdAtAsTimestamp = column[java.sql.Timestamp]("created_at")
    def extRefId = column[String]("ext_ref_id")

    def * = (createdAt, action) <> (Event.tupled, Event.unapply)
  }

  class PullRequestCommits(tag: Tag) extends Table[(Int, Int)](tag, TableNames.pullRequestCommits) {
    def pullRequestId = column[Int]("pull_request_id")
    def commitId = column[Int]("commit_id")

    def * = (pullRequestId, commitId)
    def pk = primaryKey("key", (pullRequestId, commitId))
  }

  class ProjectCommits(tag: Tag) extends Table[(Int, Int)](tag, TableNames.projectCommits) {
    def projectId = column[Int]("project_id")
    def commitId = column[Int]("commit_id")

    def * = (projectId, commitId)
    def pk = primaryKey("key", (projectId, commitId))
  }

  class ProjectMembers(tag: Tag) extends Table[(Int, Int, DateTime)](tag, TableNames.projectMembers) {
    def repoId = column[Int]("repo_id")
    def userId = column[Int]("user_id")
    def createdAt = column[DateTime]("created_at")

    def * = (repoId, userId, createdAt)
    def pk = primaryKey("key", (repoId, userId))
  }

  class IssueEvents(tag: Tag) extends Table[Event](tag, TableNames.issueEvents) {
    def eventId = column[Int]("event_id", O.PrimaryKey)
    def issueId = column[Int]("issue_id")
    def action = column[String]("action")
    def createdAt = column[DateTime]("created_at")

    def * = (createdAt, action) <> (Event.tupled, Event.unapply)
  }

  class Comments(tag: Tag) extends Table[IssueComment](tag, TableNames.comments) {
    def id = column[Int]("comment_id", O.PrimaryKey)
    def issueId = column[Int]("issue_id")
    def userId = column[Int]("user_id")
    def createdAt = column[DateTime]("created_at")
    def extRefId = column[String]("ext_ref_id")

    def * = (userId, createdAt, extRefId) <> (IssueComment.tupled, IssueComment.unapply)
  }

  class ReviewComments(tag: Tag) extends Table[ReviewComment](tag, TableNames.reviewComments) {
    def id = column[Int]("comment_id", O.PrimaryKey)
    def pullRequestId = column[Int]("pull_request_id")
    def userId = column[Int]("user_id")
    def createdAt = column[DateTime]("created_at")
    def extRefId = column[String]("ext_ref_id")

    def * = (userId, createdAt, extRefId) <> (ReviewComment.tupled, ReviewComment.unapply)
  }
}

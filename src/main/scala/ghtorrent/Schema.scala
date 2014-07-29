package ghtorrent

import git.Commit
import org.joda.time.DateTime
import DateTimeMapper._
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.lifted.ProvenShape._

object Schema {
  object Tables {
    val projects = TableQuery[Projects]
    val pullRequests = TableQuery[PullRequests]
    val users = TableQuery[Users]
    val commits = TableQuery[Commits]
    val pullRequestHistory = TableQuery[PullRequestHistory]
    val projectMembers = TableQuery[ProjectMembers]
  }

  object TableNames {
    val projects = "projects"
    val pullRequests = "pull_requests"
    val users = "users"
    val commits = "commits"
    val issues = "issues"
    val pullRequestHistory = "pull_request_history"
    val projectCommits = "project_commits"
    val projectMembers = "project_members"
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

  class PullRequestHistory(tag: Tag) extends Table[(Int, Int, String, DateTime, String)](tag, TableNames.pullRequestHistory) {
    def pullRequestId = column[Int]("pull_request_id")
    def userId = column[Int]("actor_id")
    def action = column[String]("action")
    def createdAt = column[DateTime]("created_at")
    def extRefId = column[String]("ext_ref_id")

    def * = (pullRequestId, userId, action, createdAt, extRefId)
  }

  class ProjectMembers(tag: Tag) extends Table[(Int, Int, DateTime)](tag, TableNames.projectMembers) {
    def repoId = column[Int]("repo_id")
    def userId = column[Int]("user_id")
    def createdAt = column[DateTime]("created_at")

    def * = (repoId, userId, createdAt)
    def pk = primaryKey("key", (repoId, userId))
  }
}

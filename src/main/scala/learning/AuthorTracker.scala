package learning

import ghtorrent.Schema.Tables
import git.{Commit, AuthorPullRequest}
import org.joda.time.DateTime
import scala.slick.driver.MySQLDriver.simple._

class AuthorTracker(repository: RepositoryTracker, username: String) {
  implicit lazy val session = repository.session

  lazy val ghAuthorId = getAuthorId
  lazy val coreMember = getCoreMember
  lazy val commits = getCommits
  lazy val pullRequests = getPullRequests

  private def getAuthorId: Int = {
    val authorIds = for {
      u <- Tables.users
      if u.login === username
    } yield u.id

    authorIds.first
  }

  private def getCoreMember: Option[DateTime] = {
    val coreMembers = for {
      m <- Tables.projectMembers
      if m.repoId === repository.ghRepoId
      if m.userId === ghAuthorId
    } yield m.createdAt

    coreMembers.firstOption
  }

  private def getCommits: List[Commit] = {
    val commits = for {
      // From
      pc <- Tables.projectCommits
      c <- Tables.commits
      // Join
      if c.id === pc.commitId
      // Where
      if pc.projectId === repository.ghRepoId
      if c.authorId === ghAuthorId
    } yield c

    commits.list
  }

  private def getPullRequests: List[AuthorPullRequest] = {
    val pullRequests = {
      for {
        // From
        h <- Tables.pullRequestHistory
        // Where
        if h.userId === ghAuthorId
        if h.action === "opened"
      } yield (h.pullRequestId, h.createdAt)
    }.list

    val ids = pullRequests.map(_._1)

    val mergedPullRequests = {
      for {
        // From
        h <- Tables.pullRequestHistory
        // Where
        if h.pullRequestId inSet ids
        if h.action === "merged"
      } yield (h.pullRequestId, h.createdAt)
    }.list

    pullRequests map { pr =>
      val mergedAt = mergedPullRequests.find(p => p._1 == pr._1).map(p => p._2)
      AuthorPullRequest(pr._2, mergedAt)
    }
  }
}
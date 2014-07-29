import git.PullRequest
import learning.{RepositoryTracker, PullRequestTracker}
import org.joda.time.DateTime

object Predictor {
  def main(args: Array[String]): Unit = {
    println("it works")

    val tracker = new RepositoryTracker("scala", "scala")
    val s = tracker.getSnapshots
  }
}

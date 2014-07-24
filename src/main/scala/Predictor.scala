import git.PullRequest
import learning.PullRequestTracker
import org.joda.time.DateTime

object Predictor {
  def main(args: Array[String]): Unit = {
    println("it works")

    val pull = PullRequest(1,
      "author",
      "sha",
      "source",
      "target",
      "title",
      DateTime.now.minusDays(7),
      DateTime.now,
      DateTime.now,
      1L,
      2L,
      3L,
      4L,
      5L,
      6L)

    val tracker = new PullRequestTracker(pull)
    tracker.track
  }
}

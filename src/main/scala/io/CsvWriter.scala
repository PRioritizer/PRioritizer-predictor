package io

import git.PullRequest
import learning._

object CsvWriter {

  private val escapeChars = Map("\"" -> "\"\"", "\r" -> "\\r", "\n" -> "\\n")
  private val nf = java.text.NumberFormat.getInstance(java.util.Locale.ROOT)
  nf.setMaximumFractionDigits(6)
  nf.setGroupingUsed(false)

  def write(fileName: String, data: List[(PullRequest, Important)]): Unit = {

    val header = List(
      "age",
      "title",
      "target",
      "author",
      "coreMember",
      "commitRatio",
      "pullRequestRatio",
      "comments",
      "reviewComments",
      "additions",
      "deletions",
      "commits",
      "files",
      "important")

    val rows = for {
      row <- data
      pr = row._1
      important = row._2
    } yield List(
        pr.age,
        pr.title,
        pr.target,
        pr.author,
        pr.coreMember,
        pr.contributedCommitRatio,
        pr.pullRequestAcceptRatio,
        pr.comments,
        pr.reviewComments,
        pr.linesAdded,
        pr.linesDeleted,
        pr.commits,
        pr.filesChanged,
        important)

    val contents = header :: rows
    writeData(fileName, contents)
  }

  def writeData(fileName: String, data: List[List[Any]]): Unit = {
    val contents = data.map(row => row.map(v => format(v)).mkString(",")).mkString("\n")
    writeToFile(fileName, contents)
  }

  private def writeToFile(fileName: String, contents: String): Unit = {
    val file = new java.io.File(fileName)
    val writer = new java.io.PrintWriter(file)
    try writer.write(contents) finally writer.close()
  }

  private def format(value: Any): String = value match {
    case s: String => s""""${escape(s)}""""
    case true => "1"
    case false => "0"
    case u: Unit => ""
    case b: Byte => nf.format(b)
    case c: Char => nf.format(c)
    case s: Short => nf.format(s)
    case i: Int => nf.format(i)
    case l: Long => nf.format(l)
    case f: Float => if (f.isNaN || f.isInfinity) "0" else nf.format(f)
    case d: Double => if (d.isNaN || d.isInfinity) "0" else nf.format(d)
    case _ => s""""$value""""
  }

  private def escape(value: String): String = {
    escapeChars.foldLeft(value)((s,c) => s.replace(c._1, c._2))
  }
}

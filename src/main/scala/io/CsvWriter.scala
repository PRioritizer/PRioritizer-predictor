package io

import java.io.File

import git.PullRequest
import learning._

object CsvWriter {

  private val escapeChars = Map("\"" -> "\"\"", "\r" -> "\\r", "\n" -> "\\n")
  private val nf = java.text.NumberFormat.getInstance(java.util.Locale.ROOT)
  nf.setMaximumFractionDigits(6)
  nf.setGroupingUsed(false)

  def write(file: String, data: List[(PullRequest, Important)]): Unit = write(new File(file), data)

  def write(file: File, data: List[(PullRequest, Important)]): Unit = {

    val header = List(
      "age",
      "title",
      "target",
      "author",
      "coreMember",
      "intraBranch",
      "containsFix",
      "commitRatio",
      "pullRequestRatio",
      "comments",
      "reviewComments",
      "lastCommentMention",
      "additions",
      "deletions",
      "commits",
      "files",
      "hasTestCode",
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
        pr.intraBranch,
        pr.containsFix,
        pr.contributedCommitRatio,
        pr.pullRequestAcceptRatio,
        pr.comments,
        pr.reviewComments,
        pr.lastCommentMention,
        pr.linesAdded,
        pr.linesDeleted,
        pr.commits,
        pr.filesChanged,
        pr.hasTestCode,
        important)

    val contents = header :: rows
    writeData(file, contents)
  }

  def writeData(file: File, data: List[List[Any]]): Unit = {
    val contents = data.map(row => row.map(v => format(v)).mkString(",")).mkString("\n") + "\n"
    writeToFile(file, contents)
  }

  private def writeToFile(file: File, contents: String): Unit = {
    val dir: File = file.getParentFile
    dir.mkdirs()
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

package r

import java.io.{ByteArrayOutputStream, File, PrintWriter}

import settings.RSettings

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process._

object R {
  val rscriptLocation = RSettings.rscriptLocation
  val scriptDirectory = RSettings.scriptDirectory
  val limit = RSettings.resultLimit
  val trainingScript = "train.R"
  val predictScript = "predict.R"

  def train(directory: String): Future[Boolean] = Future {
    val scriptLocation = new File(scriptDirectory, trainingScript).getPath
    val command = Seq(rscriptLocation, scriptLocation, directory)
    run(command, Some(scriptDirectory))
  }

  def predict(directory: String): Future[(Boolean, List[Double])] = Future {
    val scriptLocation = new File(scriptDirectory, predictScript).getPath
    val threshold = getThreshold
    val command = Seq(rscriptLocation, scriptLocation, directory, threshold, limit.toString)
    val (result, output, error) = runWithOutput(command, Some(scriptDirectory))

    // Parse output
    val list = if (output.trim.nonEmpty) output.trim.split('\n').map(b => b.trim.toDouble).toList else List()
    (result, list)
  }

  private def getThreshold: String = {
    val nf = java.text.NumberFormat.getInstance(java.util.Locale.ROOT)
    nf.setMaximumFractionDigits(2)
    nf.setGroupingUsed(false)
    nf.format(RSettings.probabilityThreshold)
  }

  private def runWithOutput(command: Seq[String], workingDirectory: Option[String] = None): (Boolean, String, String) = {
    val stdout = new ByteArrayOutputStream
    val stderr = new ByteArrayOutputStream
    val stdoutWriter = new PrintWriter(stdout)
    val stderrWriter = new PrintWriter(stderr)
    val process = workingDirectory
      .map(dir => Process(command, new java.io.File(dir)))
      .getOrElse(Process(command))

    // Start process
    val exitValue = process ! ProcessLogger(stdoutWriter.println, stderrWriter.println)
    stdoutWriter.close()
    stderrWriter.close()
    (exitValue == 0, stdout.toString, stderr.toString)
  }

  private def run(command: Seq[String], workingDirectory: Option[String] = None): Boolean = {
    val process = workingDirectory
      .map(dir => Process(command, new java.io.File(dir)))
      .getOrElse(Process(command))

    // Start process
    val exitValue = process.!
    exitValue == 0
  }
}

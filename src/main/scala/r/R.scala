package r

import java.io.{File, PrintWriter, ByteArrayOutputStream}
import settings.PredictorSettings
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import sys.process._

object R {
  val rscriptLocation = PredictorSettings.rscriptLocation
  val scriptDirectory = PredictorSettings.scriptDirectory
  val trainingScript = "train.R"
  val predictScript = "predict.R"

  def train(directory: String): Future[Boolean] = Future {
    val scriptLocation = new File(scriptDirectory, trainingScript).getPath
    val command = Seq(rscriptLocation, scriptLocation, directory)
    run(command, Some(scriptDirectory))
  }

  def predict(directory: String): Future[Array[Boolean]] = Future {
    val scriptLocation = new File(scriptDirectory, predictScript).getPath
    val command = Seq(rscriptLocation, scriptLocation, directory)
    Array(true, false)
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
    val logger: (String => Unit) = (s: String) => ()
    val process = workingDirectory
      .map(dir => Process(command, new java.io.File(dir)))
      .getOrElse(Process(command))

    // Start process
    val exitValue = process ! ProcessLogger(logger, logger)
    exitValue == 0
  }
}
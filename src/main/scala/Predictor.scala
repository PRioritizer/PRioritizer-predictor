import learning.TrainingData

object Predictor {
  def main(args: Array[String]): Unit = {
    println("working...")

    val data = new TrainingData("scala", "scala").get

    for {
      row <- data
      pr = row._1
      important = row._2
    } println(s"nr: ${pr.number}, age: ${pr.age}, size: ${pr.linesAdded+pr.linesDeleted}, files: ${pr.filesChanged}, important: ${important}")
  }
}

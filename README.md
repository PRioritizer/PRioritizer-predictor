PRioritizer predictor
=====================

[![Build Status](https://travis-ci.org/PRioritizer/PRioritizer-predictor.svg)](https://travis-ci.org/PRioritizer/PRioritizer-predictor)

A predictor for important pull requests. Designed to be invoked by the [analyzer](https://github.com/PRioritizer/PRioritizer-analyzer).

The predictor uses historical data of a given repository to predict if a repository requires more attention than others.
Machine learning is applied to perform the prediction. Random Forest is used as algorithm, which is implemented in R.

*Please note* that the predictor is specifically written for the [GHTorrent](http://ghtorrent.org/) project.

Prerequisites
-------------

* The [analyzer](https://github.com/PRioritizer/PRioritizer-analyzer)
* [Scala](http://www.scala-lang.org/) compiler
* [JVM 8](https://java.com/download/)
* [R](http://www.r-project.org/) environment

Building
--------

1. Clone the project into `~/predictor`
2. Install dependencies and build the project with `sbt compile`
3. Copy `src/main/resources/settings.properties.dist` to `src/main/resources/settings.properties`
4. Configure the application by editing `src/main/resources/settings.properties`
  * e.g. model directory: `~/tmp/`
  * e.g. Rscript location: `/usr/bin/Rscript`
  * e.g. script diretory: `~/predictor/R`
  * Ingore the repository settings
5. Package the project into a `.jar` file with `sbt assembly`

The predictor is now set up for use by the [analyzer](https://github.com/PRioritizer/PRioritizer-analyzer).

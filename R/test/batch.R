### Install packages
source("helper/install-packages.R")

### Include helpers
source("algorithms/all.R")
source("helper/data.R")
source("helper/evaluation.R")
source("helper/model.R")
source("helper/utils.R")

### Foreach support
load.package("foreach")

# Parallel execution
num.cores <- 2
if(.Platform$OS.type == "windows") {
  load.package("doSNOW")
  cl <- makeCluster(num.cores)
  registerDoSNOW(cl)
} else {
  load.package("doMC")
  registerDoMC(num.cores)
}

# Select algorithms
select <- list(
  logistic.regression = FALSE,
  naive.bayes = FALSE,
  random.forest = TRUE,
  support.vector.machine = FALSE
)

# Export functions
export.names <- c(
  "select",
  "printf",
  "svm",
  "randomForest",
  "naiveBayes",
  "prediction",
  "performance",
  "split.data",
  "balance.data",
  "models.evaluate",
  "svm.train",
  "svm.raw",
  "random.forest.train",
  "random.forest.raw",
  "logistic.regression.train",
  "logistic.regression.raw",
  "naive.bayes.train",
  "naive.bayes.raw",
  "prediction.performance"
)

# Test files
list <- file("test/csv/list.csv")
files <- readLines(list)
close(list)

# Run a cross validation round, return a dataframe with all results added
cross.validation <- function(model, df, runs) {
  result <- foreach(n = 1:runs, .combine = rbind, .export=export.names) %dopar% {
#  result <- foreach(n = 1:runs) %do% {
    printf("Run #%s\n", n)
    dataset <- split.data(df, .90)
    train.set <- dataset$train # balance.data(dataset$train, 1)
    res <- models.evaluate(model, train.set, dataset$test, select)
    res$run <- n
    res
  }

  result
}

# Calculate mean results after cross validation runs
cross.validation.means <- function(cvResult) {
  aggregate(cbind(auc, acc, prec, rec, f1) ~ classifier, data = cvResult, mean)
}

# ================================== PROGRAM ================================== #

foreach(f = files) %do% {
  ### Read data
  data <- read.data(f)

  ### Cross validation
  results <- cross.validation(model, data, 10)
  means <- cross.validation.means(results)
  means$file = f

  print(means)
  write.table(means, "batch.csv", col.names=FALSE, sep=",", append=TRUE)
}

# Stop parallel execution
if(.Platform$OS.type == "windows") {
  stopCluster(cl)
}

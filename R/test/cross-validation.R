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

# Run a cross validation round, return a dataframe with all results added
cross.validation <- function(model, df, runs) {
  result <- foreach(n = 1:runs, .combine = rbind, .export=export.names) %dopar% {
    printf("Run #%s\n", n)
    dataset <- split.data(df, .90)
    res <- models.evaluate(model, dataset$train, dataset$test, select)
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

### Read data
data <- read.data("test/csv/angular.csv")

### Cross validation
results <- cross.validation(model, data, 10)
means <- cross.validation.means(results)
printf("=====================================================================\n")
printf("                                  Runs\n")
printf("=====================================================================\n")
print(results)
printf("=====================================================================\n")
printf("                                  Mean\n")
printf("=====================================================================\n")
print(means)

# Data characteristics
num.true <- nrow(subset(data, important == TRUE))
num.false <- nrow(subset(data, important == FALSE))
printf("Distribution: %sF:%sT (Total: %s)\n", num.false, num.true, nrow(data))

# Stop parallel execution
if(.Platform$OS.type == "windows") {
  stopCluster(cl)
}

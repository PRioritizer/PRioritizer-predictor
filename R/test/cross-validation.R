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

# Select algorithms
select <- list(
  logistic.regression = FALSE,
  naive.bayes = FALSE,
  random.forest = TRUE
)

# Run a cross validation round, return a dataframe with all results added
cross.validation <- function(model, df, runs) {
  result <- foreach(n = 1:runs, .combine = rbind) %do% {
    printf("Run #%s\n", n)
    dataset <- split.data(df, .75)
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

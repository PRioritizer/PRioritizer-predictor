# Run in console:
# source("./predictor/R/classification.R")

library("randomForest") # Random forest
library("e1071") # Bayes
library("ROCR") # Prediction
library("foreach") # Parallel foreach

# Utils
printf <- function(...) cat(sprintf(...))

# Train and test models
models.train <- function(model, train, test) {
  # Results
  results = data.frame(classifier = rep(NA, 3),
    auc  = rep(0, 3),
    acc  = rep(0, 3),
    prec = rep(0, 3),
    rec  = rep(0, 3),
    f1   = rep(0, 3),
    stringsAsFactors = FALSE)

  ### Binary logistic regression
  logmodel <- binlog.train(model, train)
  predictions <- predict(logmodel, newdata = test)
  pred.obj <- prediction(predictions, test$important)
  metrics <- classification.perf.metrics("BinLogReg", pred.obj)
  results[1,] <- data.frame(metrics$classifier, metrics$auc, metrics$acc, metrics$prec, metrics$rec, metrics$f1, stringsAsFactors=FALSE)

  ### Naive Bayes
  nbmodel <- bayes.train(model, train)
  predictions <- predict(nbmodel, newdata = test, type = "raw")
  pred.obj <- prediction(predictions[,2], test$important)
  metrics <- classification.perf.metrics("NaiveBayes", pred.obj)
  results[2,] <- data.frame(metrics$classifier, metrics$auc, metrics$acc, metrics$prec, metrics$rec, metrics$f1, stringsAsFactors=FALSE)

  ### Random Forest
  rfmodel <- rf.train(model, train)
  predictions <- predict(rfmodel, test, type = "prob")
  pred.obj <- prediction(predictions[,2], test$important)
  metrics <- classification.perf.metrics("RandomForest", pred.obj)
  results[3,] <- data.frame(metrics$classifier, metrics$auc, metrics$acc, metrics$prec, metrics$rec, metrics$f1, stringsAsFactors=FALSE)

  results
}

binlog.train <- function(model, train.set) {
  binlog <- glm(model, data = train.set, family = "binomial");
  # print(summary(binlog))
  binlog
}

bayes.train <- function(model, train.set) {
  bayesModel <- naiveBayes(model, data = train.set)
  # print(summary(bayesModel))
  # print(bayesModel)
  bayesModel
}

rf.train <- function(model, train.set) {
  rfmodel <- randomForest(model, data = train.set, importance = TRUE)
  # print(rfmodel)
  # print(importance(rfmodel))
  # varImpPlot(rfmodel, type = 1)
  # varImpPlot(rfmodel, type = 2)
  # plot(rfmodel)
  rfmodel
}

# Returns a list l where
# l[1] training dataset
# l[2] testing dataset
split.data <- function(data, split = .5) {
  # Shuffle data
  newData <- data[sample.int(nrow(data)),]

  # TODO: split such that all targets are present in first set

  # Split data into training and test data
  newData.train <- data[1:floor(nrow(data)*split), ]
  newData.test <- data[(floor(nrow(data)*split)+1):nrow(data), ]
  list(train = newData.train, test = newData.test)
}

prepare.data <- function(data) {
  # Drop string columns
  dropCols <- names(data) %in% c("title", "author", "target")
  newData <- data[!dropCols]

  # Convert columns to booleans
  newData$coreMember <- newData$coreMember == 1
  newData$important <- newData$important == 1

  # Convert to factor columns
  newData$coreMember <- factor(newData$coreMember)
  newData$important <- factor(newData$important)

  newData
}

# Run a cross validation round, return a dataframe with all results added
# sampler is f: data.frame -> Int -> list
# classifier is f: data.frame -> Int -> list
cross.validation <- function(model, df, runs = 10) {
  result <- foreach(n = 1:runs, .combine = rbind) %dopar% {
              dataset <- split.data(df, .75)
              interm <- models.train(model, dataset$train, dataset$test)
              interm$run <- n
              interm
  }

  result
}

# Calculate mean results after cross validation runs
cross.validation.means <- function(cvResult) {
  aggregate(cbind(auc, acc, prec, rec, f1) ~ classifier, data = cvResult, mean)
}

# Extract classification metrics from a ROCR prediction object
classification.perf.metrics <- function(classif, pred.obj) {
  p1 <- performance(pred.obj, "acc")
  p2 <- performance(pred.obj, "prec", "rec")

  auc  <- as.numeric(performance(pred.obj,"auc")@y.values)
  acc  <- median(Filter(function(x){is.finite(x)}, unlist(p1@y.values)))
  prec <- median(Filter(function(x){is.finite(x)}, unlist(p2@y.values)))
  rec  <- median(Filter(function(x){is.finite(x)}, unlist(p2@x.values)))
  f1   <- 2*prec*rec / (prec + rec)

  list(classifier = classif,
       auc  = auc,
       acc  = acc,
       prec = prec,
       rec  = rec,
       f1   = f1)
}

read.train.data <- function(file) {
  # Read and transform input data
  input <- read.csv(file = file, header = TRUE)
  data <- prepare.data(input)
  data
}

data <- read.train.data("~/git/prioritizer/predictor/xbmc-100.csv")
model <- important ~ age + coreMember +
                     commitRatio + pullRequestRatio +
                     comments + reviewComments +
                     additions + deletions +
                     commits + files


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

### Single run
# data <- split.data(data, .75)
# results <- models.train(model, data$train, data$test)
# print(results)

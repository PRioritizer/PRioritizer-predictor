# Evaluation
load.package("ROCR") # Prediction

# Train and test models
models.evaluate <- function(model, train, test, select = list()) {
  # Number of algorithms
  n <- 4

  # Results
  results = data.frame(classifier = rep(NA, n),
    auc  = rep(0, n),
    acc  = rep(0, n),
    prec = rep(0, n),
    rec  = rep(0, n),
    f1   = rep(0, n),
    stringsAsFactors = FALSE)

  ### Binary logistic regression
  if (length(select) == 0 || select$logistic.regression) {
    lrmodel <- logistic.regression.train(model, train)
    predictions <- logistic.regression.raw(lrmodel, test)
    results[1,] <- prediction.performance("LogReg", predictions, test)
  }

  ### Naive Bayes
  if (length(select) == 0 || select$naive.bayes) {
    nbmodel <- naive.bayes.train(model, train)
    predictions <- naive.bayes.raw(nbmodel, test)
    results[2,] <- prediction.performance("NaiveBayes", predictions, test)
  }

  ### Random Forest
  if (length(select) == 0 || select$random.forest) {
    rfmodel <- random.forest.train(model, train)
    predictions <- random.forest.raw(rfmodel, test)
    results[3,] <- prediction.performance("RandomForest", predictions, test)
  }

  ### Support Vector Machine
  if (length(select) == 0 || select$support.vector.machine) {
    svmmodel <- svm.train(model, train)
    predictions <- svm.raw(svmmodel, test)
    results[4,] <- prediction.performance("SVM", predictions, test)
  }

  results
}

# Get the performance results of the predictions
prediction.performance <- function(classifier, predictions, test) {
  pred.obj <- prediction(predictions, test$important)

  p1 <- performance(pred.obj, "acc")
  p2 <- performance(pred.obj, "prec", "rec")

  auc  <- as.numeric(performance(pred.obj, "auc")@y.values)
  acc  <- median(Filter(function(x){is.finite(x)}, unlist(p1@y.values)))
  prec <- median(Filter(function(x){is.finite(x)}, unlist(p2@y.values)))
  rec  <- median(Filter(function(x){is.finite(x)}, unlist(p2@x.values)))
  f1   <- 2*prec*rec / (prec + rec)

  data.frame(classifier, auc, acc, prec, rec, f1, stringsAsFactors=FALSE)
}

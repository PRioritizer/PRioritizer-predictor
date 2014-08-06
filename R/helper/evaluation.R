# Evaluation
suppressPackageStartupMessages(library("ROCR")) # Prediction

# Train and test models
models.evaluate <- function(model, train, test) {
  # Results
  results = data.frame(classifier = rep(NA, 3),
    auc  = rep(0, 3),
    acc  = rep(0, 3),
    prec = rep(0, 3),
    rec  = rep(0, 3),
    f1   = rep(0, 3),
    stringsAsFactors = FALSE)


  ### Binary logistic regression
  lrmodel <- logistic.regression.train(model, train)
  predictions <- logistic.regression.raw(lrmodel, test)
  results[1,] <- prediction.performance("LogReg", predictions, test)

  ### Naive Bayes
  nbmodel <- naive.bayes.train(model, train)
  predictions <- naive.bayes.raw(nbmodel, test)
  results[2,] <- prediction.performance("NaiveBayes", predictions, test)

  ### Random Forest
  rfmodel <- random.forest.train(model, train)
  predictions <- random.forest.raw(rfmodel, test)
  results[3,] <- prediction.performance("RandomForest", predictions, test)

  results
}

# Get the performance results of the predictions
prediction.performance <- function(classifier, predictions, test) {
  pred.obj <- prediction(predictions, test$important)

  p1 <- performance(pred.obj, "acc")
  p2 <- performance(pred.obj, "prec", "rec")

  auc  <- as.numeric(performance(pred.obj,"auc")@y.values)
  acc  <- median(Filter(function(x){is.finite(x)}, unlist(p1@y.values)))
  prec <- median(Filter(function(x){is.finite(x)}, unlist(p2@y.values)))
  rec  <- median(Filter(function(x){is.finite(x)}, unlist(p2@x.values)))
  f1   <- 2*prec*rec / (prec + rec)

  data.frame(classifier, auc, acc, prec, rec, f1, stringsAsFactors=FALSE)
}

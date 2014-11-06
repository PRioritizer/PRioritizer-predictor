# Logistic regression

logistic.regression.train <- function(model, train.set) {
  lr.model <- model

  # Remove factors with less than 2 unique values
  if (length(unique(train.set$coreMember)) < 2) {
    lr.model <- update(model, . ~ . - coreMember)
  }

  if (length(unique(train.set$containsFix)) < 2) {
    lr.model <- update(model, . ~ . - containsFix)
  }

  if (length(unique(train.set$lastCommentMention)) < 2) {
    lr.model <- update(model, . ~ . - lastCommentMention)
  }

  binlog <- glm(lr.model, data = train.set, family = "binomial")
  # print(summary(binlog))
  binlog
}

logistic.regression.raw <- function(trained.model, test.set) {
  predictions <- predict(trained.model, newdata = test.set, type = "response")
  predictions
}

logistic.regression.predict <- function(trained.model, test.set, threshold = 0.5, limit = -1) {
  predictions <- logistic.regression.raw(trained.model, test.set)
  predictions <- as.boolean.factor(predictions, threshold, limit) # Convert to boolean
  predictions
}

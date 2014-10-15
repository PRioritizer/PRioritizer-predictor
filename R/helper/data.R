# Data

# Returns a list l where
# l[1] training dataset
# l[2] testing dataset
split.data <- function(data, split = .5) {
  # Shuffle data
  n <- nrow(data)
  newData <- data[sample.int(nrow(data)),]

  # TODO: split such that all targets are present in first set

  # Split data into training and test data
  newData.train <- newData[1:floor(n*split), ]
  newData.test <- newData[(floor(n*split)+1):n, ]
  list(train = newData.train, test = newData.test)
}

prepare.data <- function(data) {
  # Remove string columns
  drop <- c("title", "target", "author")
  data[,!(names(data) %in% drop)]

  # Calculate churn
  data$churn <- data$additions + data$deletions

  # Convert columns to boolean factors
  data$coreMember <- as.boolean.factor(data$coreMember)
  data$important <- as.boolean.factor(data$important)
  data$containsFix <- as.boolean.factor(data$containsFix)
  data$lastCommentMention <- as.boolean.factor(data$lastCommentMention)

  data
}

read.data <- function(file) {
  # Read and transform input data
  input <- read.csv(file = file, header = TRUE)
  data <- prepare.data(input)
  data
}

as.boolean.factor <- function(list, threshold = 0.5, limit = -1) {
  # Total length
  size <- length(list)

  # Hard limit to 1/3 of size
  if (limit > -1) {
    limit <- min(limit, floor(size/3))
  }

  # Limit number of TRUE values
  if (limit > -1 && limit < size) {
    # Get sorted indices
    indices <- sort.int(list, decreasing = TRUE, index.return = TRUE)$ix

    # Set values outside limit to probability: 0
    for (i in (limit+1):size ) {
      list[indices[i]] = 0
    }
  }

  # Convert columns to booleans
  list <- list >= threshold
  # Convert to factor columns
  list <- factor(list, c(FALSE, TRUE))
  # Return
  list
}

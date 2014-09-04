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
  # Drop string columns
  dropCols <- names(data) %in% c("title", "author", "target")
  newData <- data[!dropCols]

  # Convert columns to boolean factors
  newData$coreMember <- as.boolean.factor(newData$coreMember)
  newData$important <- as.boolean.factor(newData$important)

  newData
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

  # Limit number of TRUE values
  if (limit > -1 && limit < size) {
    # Hard limit to 1/3 of size
    limit <- min(limit, size/3)

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

# Data

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

read.data <- function(file) {
  # Read and transform input data
  input <- read.csv(file = file, header = TRUE)
  data <- prepare.data(input)
  data
}

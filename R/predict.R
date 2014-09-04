#!/bin/Rscript

### Install packages
source("helper/install-packages.R")

### Include helpers
source("algorithms/random-forest.R")
source("helper/data.R")
source("helper/model.R")
source("helper/utils.R")

# ================================== PROGRAM ================================== #

args <- commandArgs(TRUE)
argc <- length(args)

if (argc < 1 || argc > 3) {
  stop("Expected one, two or three arguments.\nUsage: Rscript train.R path/to/model [threshold] [limit]")
}

### Threshold
threshold <- ifelse(argc == 2, as.numeric(args[2]), 0.5)

### Limit
limit <- ifelse(argc == 3, as.numeric(args[3]), -1)

### Files
dir <- args[1]
input.file <- file.path(dir, "input.csv")
model.file <- file.path(dir, "model.RData")

input <- read.data(input.file)

### Read trained model (access via the trained.model variable)
load(model.file)

### Predict value
predictions <- random.forest.predict(trained.model, input, threshold, limit)
predictions <- as.logical(predictions) # Convert to booleans
output <- paste(predictions, collapse="\n")
printf("%s\n", output)

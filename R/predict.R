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

if (argc != 1 && argc != 2) {
  stop("Expected one or two arguments.\nUsage: Rscript train.R <dir> [<threshold>]")
}

### Threshold
threshold <- ifelse(argc == 2, as.numeric(args[2]), 0.5)

### Files
dir <- args[1]
input.file <- paste(dir, "input.csv", sep = "/")
model.file <- paste(dir, "model.RData", sep = "/")

input <- read.data(input.file)

### Read trained model (access via the trained.model variable)
load(model.file)

### Predict value
predictions <- random.forest.predict(trained.model, input, threshold)
predictions <- as.logical(predictions) # Convert to booleans
output <- paste(predictions, collapse="\n")
printf("%s\n", output)

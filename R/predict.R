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

if (length(args) != 1) {
  stop("Expected one argument.\nUsage: Rscript train.R <dir>")
}

### Files
dir <- args[1]
input.file <- paste(dir, "input.csv", sep = "/")
model.file <- paste(dir, "model.RData", sep = "/")

input <- read.data(input.file)
data2 <- prepare.data(input)

### Read trained model (access via the trained.model variable)
load(model.file)

### Predict value
predictions <- random.forest.predict(trained.model, data2)
predictions <- as.logical(predictions) # Convert to booleans
output <- paste(predictions, collapse="\n")
printf("%s\n", output)

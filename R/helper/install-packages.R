# Packages

load.package <- function(package, user.lib = TRUE) {
  if (!package %in% installed.packages()) {
    if (user.lib)
      install.packages(package, lib = Sys.getenv("R_LIBS_USER"))
    else
      install.packages(package)
  }

  # Don't show package messages
  suppressPackageStartupMessages(library(package, character.only = TRUE))
}

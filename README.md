PRioritizer predictor
=====================

[![Build Status](https://magnum.travis-ci.com/erikvdv1/PRioritizer-predictor.svg?token=wgtEsFC7Tpxoy7U9Y5p1&branch=master)](https://magnum.travis-ci.com/erikvdv1/PRioritizer-predictor)

A predictor for important pull requests. Designed to be invoked by the [analyzer](https://github.com/erikvdv1/PRioritizer-analyzer).

The predictor uses historical data of a given repository to predict if a repository requires more attention than others.
Machine learning is applied to perform the prediction. Random Forest is used as algorithm, which is implemented in R.

The predictor is specifically written for the [GHTorrent](http://ghtorrent.org/) project.

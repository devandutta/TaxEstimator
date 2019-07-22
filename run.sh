#!/bin/bash
# TaxEstimator main run script, written by Devan Dutta
# July 2019
javac -cp ".:./json-simple-1.1.1.jar" TaxEstimator.java

java -cp ".:./json-simple-1.1.1.jar" TaxEstimator

# Welcome

Welcome to the Tax Estimator!

# Author
Devan Dutta

# What is it?

This is a lightweight Java app that I built in July 2019 as a tool for myself
for a couple reasons:
1. I recently graduated college and figured it was time for me to learn how to calculate taxes. After I learned how to do that, I figured "Why not automate this?"

2. I wanted to re-learn some Java skills, since I hadn't worked with Java since
high school. I also learned how to download external files and parse JSON in
Java while working on this project.

# How to run the app
1. Run `chmod u+x run.sh` (if you don't have execute permission for the run script).

2. To run the app, you have a couple options:
  * Option 1: Run `make` in the app directory
  * Option 2: Run `./run.sh`

# Source Acknowledgements
* The tax bracket data that I use in my app came from taxee's
[taxee-tax-statistics](https://github.com/taxee/taxee-tax-statistics "Tax Statistics") repository on Github. Taxee's developers did an excellent job of
manually creating json files containing the federal tax brackets and all
states' tax brackets for multiple years.
  * Taxee also has their own [API](https://taxee.io).

* The JSON library I used was [json-simple] (https://github.com/fangyidong/json-simple).
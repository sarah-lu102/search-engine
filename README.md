# COMP 4321 - Search Engines Project

Final Project for COMP 4321

## Installation

Install Maven on the machine.

Either:

Follow this [guide](https://www.javahelps.com/2017/10/install-apache-maven-on-linux.html) to set up

Or

use command:

```bash
sudo yum install maven
```

Double check all the paths are correct

Run:

```bash
mvn -version
```
to check maven is installed correctly



## Usage

Move into the folder 'searchengine'
```bash
cd searchengine
```

To compile run:

```bash
mvn compile
```

To the run the Spider Crawler run:

```bash
mvn exec:java -Dexec.mainclass=SE.Crawler
```

To run the Spider Test run:
```bash
mvn exec:java -Dexec.mainclass=SEtests.SpiderTest
```


## Contributing
Jia (Sarah) Lu
Adam Feuer
Sam Baltrus

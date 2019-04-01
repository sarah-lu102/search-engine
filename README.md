# COMP 4321 - Search Engines Project

Final Project for COMP 4321 - Group 4

Contributors: Jia Lu, Sam Baltrus, Adam Feuer

## Documents
* DB files can be found in the "rocksDB files" folder. The DB which contains the indexed 30 pages starting from http://www.cse.ust.hk/can be found in the "pageIDToURL" folder. 
* spider_result.txt and the PDF of the RocksDB scheme can be found in the current directory.
* source code of spider program: searchengine/src/main/java/SE/Crawler.java
* source code of test program: searchengine/src/main/java/SEtests/SpiderTest.java

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
mvn exec:java -Dexec.mainClass=SE.Crawler
```

To run the Spider Test run:
```bash
mvn exec:java -Dexec.mainClass=SEtests.SpiderTest
```


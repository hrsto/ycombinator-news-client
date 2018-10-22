#!/bin/bash

opts="-B"

#options for main(String[] args)
runtimeOpts=""

case $1 in
    "build")
        mvn $opts clean package
    ;;
    "justrun")
        mvn $opts package -DskipTests
        java -jar main/target/ycombinator-news-client-1.0-SNAPSHOT.jar $runtimeOpts
    ;;
    "test")
        mvn $opts clean test
    ;;
    "clean")
        mvn $opts clean
    ;;
    "verify")
        mvn $opts -B verify
    ;;
    "execute")
        java -jar main/target/ycombinator-news-client-1.0-SNAPSHOT.jar $runtimeOpts
    ;;
    *) echo "Supply build|justrun|test|clean|verify|execute"
    ;;
esac

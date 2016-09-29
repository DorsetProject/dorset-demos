#!/bin/bash

DIR=`dirname $0`
JAR=$(find $DIR/target/ -name 'slackbot*.jar')
java -jar $JAR -d $JAR


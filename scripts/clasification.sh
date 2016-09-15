#!/bin/bash
JAR_PATH=$HOME/workspace/E-CO-2/Classification/target/Classification-1.0-SNAPSHOT-jar-with-dependencies.jar
STOPWORDS=$HOME/workspace/E-CO-2/etc/stopwords.csv
MODEL_PATH=$HOME/workspace/E-CO-2/etc/model
PROPS_FILE=$HOME/workspace/E-CO-2/etc/classification.properties
TAGGER_FILE=$HOME/workspace/E-CO-2/etc/model/stanford/english-left3words-distsim.tagger
CATEGORIES_FOLDER=$HOME/workspace/E-CO-2/Competences

find $CATEGORIES_FOLDER -name '*.csv' -exec cat {} \; > $HOME/workspace/E-CO-2/etc/allTerms.csv
ALL_TERMS=$HOME/workspace/E-CO-2/etc/allTerms.csv
cat $ALL_TERMS | awk 'BEGIN{FS=","} {print $1}' > tmp
sort -r tmp | uniq > $ALL_TERMS



echo "time  hadoop jar $JAR_PATH -op c -i $1 -o $2 -c $CATEGORIES_FOLDER -p $PROPS_FILE"



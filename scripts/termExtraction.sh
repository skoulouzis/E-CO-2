#!/bin/bash
JAR_PATH=$HOME/workspace/E-CO-2/Traning/target/Traning-1.0-SNAPSHOT-jar-with-dependencies.jar
DICTIONARY_ALL=$HOME/workspace/E-CO-2/etc/dictionaryAll.csv 
STOPWORDS=$HOME/workspace/E-CO-2/etc/stopwords.csv
MODEL_PATH=$HOME/workspace/E-CO-2/etc/model
PROPS_FILE=$HOME/workspace/E-CO-2/etc/configure.properties
TAGGER_FILE=$HOME/workspace/E-CO-2/etc/model/stanford/english-left3words-distsim.tagger

TRAIN_DOC_PATHS=()
CATEGORIES_FOLDER=$1
for i in $(ls -d $CATEGORIES_FOLDER/*)
do 
  TRAIN_DOC_PATHS+=($i)
done

for i in "${TRAIN_DOC_PATHS[@]}"
do
  for f in $i/*.txt
  do
    java -Dstop.words.file=$STOPWORDS -Ditemset.file=$DICTIONARY_ALL -Dmodel.path=$MODEL_PATH -Dtagger.file=$TAGGER_FILE -jar $JAR_PATH -op x -i $f -o $i/terms.csv -p $PROPS_FILE
  done
done
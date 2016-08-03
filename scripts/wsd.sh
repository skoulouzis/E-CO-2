#!/bin/bash
JAR_PATH=$HOME/workspace/E-CO-2/Traning/target/Traning-1.0-SNAPSHOT-jar-with-dependencies.jar
DICTIONARY_ALL=$HOME/workspace/E-CO-2/etc/dictionaryAll.csv 
STOPWORDS=$HOME/workspace/E-CO-2/etc/stopwords.csv
MODEL_PATH=$HOME/workspace/E-CO-2/etc/model
PROPS_FILE=$HOME/workspace/E-CO-2/etc/configure.properties

TRAIN_DOC_PATHS=()
CATEGORIES_FOLDER=$1
for i in $(ls -d $CATEGORIES_FOLDER/*)
do 
  TRAIN_DOC_PATHS+=($i)
done

for i in "${TRAIN_DOC_PATHS[@]}"
do
  for f in $i/*.csv
  do
    base=`basename $i`
    echo $base
    screen -dmSL $base java -Xmx2g -Dstop.words.file=$STOPWORDS -Ditemset.file=$DICTIONARY_ALL -Dmodel.path=$MODEL_PATH -Dnum.of.terms=500 -Doffset.terms=1 -jar $JAR_PATH -op w -i $f -o $i/$base.avro -p $PROPS_FILE
    screen -dmSL $base java -Xmx2g -Dstop.words.file=$STOPWORDS -Ditemset.file=$DICTIONARY_ALL -Dmodel.path=$MODEL_PATH -Dnum.of.terms=500 -Doffset.terms=500 -jar $JAR_PATH -op w -i $f -o $i/$base.avro -p $PROPS_FILE
  done
done

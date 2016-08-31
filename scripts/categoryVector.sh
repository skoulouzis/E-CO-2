#!/bin/bash
JAR_PATH=$HOME/workspace/E-CO-2/Traning/target/Traning-1.0-SNAPSHOT-jar-with-dependencies.jar
DICTIONARY_ALL=$HOME/workspace/E-CO-2/etc/itemset.csv
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
  for f in $i/*.avro
  do
    base=`basename $i`
    echo $base
    echo "screen -dmSL $base  nice -n 15 java -Dstop.words.file=$STOPWORDS -Ditemset.file=$DICTIONARY_ALL -jar $JAR_PATH -op t -i $f -o $i/$base.csv -p $PROPS_FILE"
  done
done



screen -ls | grep Detached | cut -d. -f1 | awk '{print $1}' > pids

while read p; do
  cpulimit -p $p -l 20 &
done < pids
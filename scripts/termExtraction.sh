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



preserve -list |grep $USER | awk '{print $1}' > reservations


while read p; do
  res+=($p)
done < reservations

index=0
resLen=${#res[@]}
for i in "${TRAIN_DOC_PATHS[@]}"
do
  for f in $i/*.txt
  do
        index=$((index+1))
        base=`basename $i`
        if [ "$index" -ge "$resLen" ];
        then
                index=0
        fi
        #echo index $index
        #echo res $res
        #echo len $resLen
        echo "screen -dmSL $base prun -reserve ${res[$index]} -Dstop.words.file=$STOPWORDS -Ditemset.file=$DICTIONARY_ALL -Dmodel.path=$MODEL_PATH -Dtagger.file=$TAGGER_FILE -jar $JAR_PATH -op x -i $i -o $i/terms.csv -p $PROPS_FILE"
  done
done
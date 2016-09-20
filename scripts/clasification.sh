#!/bin/bash
JAR_PATH=$HOME/workspace/E-CO-2/Classification/target/Classification-1.0-SNAPSHOT-jar-with-dependencies.jar
STOPWORDS=$HOME/workspace/E-CO-2/etc/stopwords.csv
MODEL_PATH=$HOME/workspace/E-CO-2/etc/model
PROPS_FILE=$HOME/workspace/E-CO-2/etc/classification.properties
TAGGER_FILE=$HOME/workspace/E-CO-2/etc/model/stanford/english-left3words-distsim.tagger
CATEGORIES_FOLDER=$1

find $CATEGORIES_FOLDER -name '*.csv' -exec cat {} \; > $HOME/workspace/E-CO-2/etc/allTerms.csv
ALL_TERMS=$HOME/workspace/E-CO-2/etc/allTerms.csv
cat $ALL_TERMS | awk 'BEGIN{FS=","} {print $1}' > tmp
sort -r tmp | uniq > $ALL_TERMS

preserve -list | grep $USER |  grep -i r | awk '{print $1}' > reservations


while read p; do
  res+=($p)
done < reservations

index=0
resLen=${#res[@]}
FILECOUNT=$(find $2 -type f | wc -l)
FILE_PER_JOB=$(( FILECOUNT / resLen))

rm -r /tmp/classification_*
for ((i = 0 ; i < $resLen ; i++)); do
  mkdir /tmp/classification_$i
  for f in $2/*.txt
  do
    if [ "$index" -ge "$FILE_PER_JOB" ];
    then
      index=0
      break
    fi
    cp $f /tmp/classification_$i
    index=$((index+1))
  done
done



for ((i = 0 ; i < $resLen; i++)); do
  folder=$((i+1))
#   echo "run in ${res[$i]} classification_$i"
  screen -dmSL jobs prun -reserve ${res[$i]} -np 1  time  java -Dstop.words.file=$HOME/workspace/E-CO-2/etc/stopwords.csv -Ditemset.file=$HOME/workspace/E-CO-2/etc/allTerms.csv -Dmodel.path=$HOME/workspace/E-CO-2/etc/model/ -jar $HOME/workspace/E-CO-2/Classification/target/Classification-1.0-SNAPSHOT-jar-with-dependencies.jar -op c -i /tmp/classification_$i -o $3 -c $HOME/workspace/E-CO-2/Categories -p $HOME/workspace/E-CO-2/etc/classification.properties
done

screen -dmSL jobs prun -reserve ${res[0]} -np 1  time  java -Dstop.words.file=$HOME/workspace/E-CO-2/etc/stopwords.csv -Ditemset.file=$HOME/workspace/E-CO-2/etc/allTerms.csv -Dmodel.path=$HOME/workspace/E-CO-2/etc/model/ -jar $HOME/workspace/E-CO-2/Classification/target/Classification-1.0-SNAPSHOT-jar-with-dependencies.jar -op c -i /tmp/classification_$resLen -o $3 -c $HOME/workspace/E-CO-2/Categories -p $HOME/workspace/E-CO-2/etc/classification.properties
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


NUM_OF_TERMS=300;
for i in "${TRAIN_DOC_PATHS[@]}"
do
  for f in $i/*.csv
  do
    base=`basename $i`
#     echo $base
#     echo $f
    NUM_OF_LINES=`cat $f | wc -l`
    echo $NUM_OF_LINES
    for ((x = 0 ; x <= $NUM_OF_LINES ; x=x+$NUM_OF_TERMS)); do
         java -Xmx2g -Dstop.words.file=$STOPWORDS -Ditemset.file=$DICTIONARY_ALL -Dmodel.path=$MODEL_PATH -Dnum.of.terms=$NUM_OF_TERMS -Doffset.terms=$x -jar $JAR_PATH -op w -i $f -o $i/$base.avro -p $PROPS_FILE &
         pid=$!
         cpulimit -p $pid -l 10 &
	
	CPU_USAGE=$[100-$(vmstat|tail -1|awk '{print $15}')]
	CPU_USAGE=${CPU_USAGE%.*}
	while [ $CPU_USAGE -gt 90 ]; do
	  echo "CPU: $CPU_USAGE seeling" 
	  sleep 10;
	  CPU_USAGE=$[100-$(vmstat|tail -1|awk '{print $15}')]
	  CPU_USAGE=${CPU_USAGE%.*}
	done
	
      done
    done
done
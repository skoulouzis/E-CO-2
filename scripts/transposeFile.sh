#!/bin/bash

for i in $(ls -d $1/*)
do
  echo transposing $i
  awk '
  { 
      for (i=1; i<=NF; i++)  {
	  a[NR,i] = $i
      }
  }
  NF>p { p = NF }
  END {    
      for(j=1; j<=p; j++) {
	  str=a[1,j]
	  for(i=2; i<=NR; i++){
	      str=str" "a[i,j];
	  }
	  print str
      }
  }' $i > tmpFile 
  
  mv tmpFile $i.trans
done

rm tmpFile


index=0
for i in $(ls -d $1/*.trans)
do
  if [ "$index" -eq 0 ];
  then
    header=`head -n 1 $i`
    echo "fileName "$header > all.csv
  fi
  filename=$(basename "$i")
  extension="${filename##*.}"
  filename="${filename%.*}"
  line=`tail -n +2 $i`
  echo $filename $line >> all.csv
  index=$((index+1))
  rm $i
done




 sed  -i "s/ /,/g" all.csv

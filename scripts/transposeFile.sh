#!/bin/bash


transpose(){
  echo transposing $i
  sort $i > tmp
  mv tmp $i
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
  }' $1 > tmpFile

  mv tmpFile $1.trans
  #rm tmpFile
}

for i in $(ls -d $1/*)
do
  transpose $i
done




index=0
for i in $(ls -d $1/*.trans)

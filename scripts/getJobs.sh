#!/bin/bash

step=100

for i in $(seq  1 $step 1000)
do
  echo page num. $i
  end=$(($i + step))
  curl -i -A "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"  -H "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36" "https://www.indeed.com/jobs?q=data+scientist&start=$i&limit=$step" &> response.html
  grep "href=\"/rc/clk?jk=" response.html | grep -oP '.*?"\K[^"]+(?=")' | grep jk >> ids
  sleep $[ ( $RANDOM % 100 )  + 1 ]s
done

while read p; do
  id=`echo $p | grep -o 'jk=.*&' | sed 's/\(jk=\|&\)//g'`
    if [ ! -f job_$id.txt ]; then
    curl -Ls -H "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36" "https://www.indeed.com$p" &> job_$id.html
    w3m job_$id.html  -dump -T text/html > job_$id.txt
    echo "Saved job_$id.txt"
    sleep $[ ( $RANDOM % 10 )  + 1 ]s
  fi
done <ids

# 
# 
# 
name=`date +%s`
mkdir $name 

for dir in job_*.txt; do
  b=`stat -c %s "$dir"`
  if [ $b -ge 2000 ] ; then
    mv $dir $name
  else
    rm $dir
  fi
done





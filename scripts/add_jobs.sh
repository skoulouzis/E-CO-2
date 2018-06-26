#!/bin/bash


# 
# id=0
# mkdir /tmp/jobs
# for f in $1/*.json; do
#     id=$((id+1))
#     cont=`jq -r .description $f`
#     jq --null-input --compact-output --arg cont "$cont" --arg id "$id" '{"id":$id,"contents": $cont}' > /tmp/jobs/$id.json
# done



for f in /tmp/jobs/*.json; do
    curl --request POST --url 'http://localhost:9999/e-co2/classification/job' --header 'Content-Type: application/json' -d @$f
done
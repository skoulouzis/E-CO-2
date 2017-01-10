#!/bin/bash

HOST=localhost
PORT=9999

curl -X GET http://$HOST:$PORT/e-co2/average/course | jq . > courseAvg.json
curl -X GET http://$HOST:$PORT/e-co2/average/job | jq . > jobAvg.json
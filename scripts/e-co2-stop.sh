#!/bin/bash

pid=`ps aux | grep rest-1.0-SNAPSHOT-jar-with-dependencies | awk '{print $2}'`
kill -9 $pid

#!/bin/bash

#run_taufail.sh <stack size> <heap size> <path/> <tau>

javac -cp "lib/*" -d bin src/*.java

tau=$4
graph_path=$3/
pat1=.sorted
log_path=./log/

for graph in $graph_path*$pat1
do
    name=$(basename $graph $pat1)
    echo $name
    java -Xss$1g -Xmx$2g -cp "bin":"lib/*" TauFail $name $tau>  $log_path$name.taufailres
done
rm $graph_path*_temp_taufail.*

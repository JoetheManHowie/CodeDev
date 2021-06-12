#!/bin/bash

#run_alg2.sh <stack size> <heap size> <path/>

javac -cp "lib/*" -d bin src/*.java

graph_path=$3/
pat1=.sorted

for graph in $graph_path*$pat1
do
    name=$(basename $graph $pat1)
    echo $name
    java -Xss$1g -Xmx$2g -cp "bin":"lib/*" Alg2 $name   
done
rm $graph_path*_temp*

#!/bin/bash

#run_coarsen.sh <stack size> <heap size> <r>

echo Compiling ...

javac -cp "lib/*" -d bin src/*.java

echo Code Successfully Compiled!

graph_path=./graphs/
log_path=./log/
pat1=.graph

for graph in $graph_path*$pat1
do
    name=$(basename $graph $pat1)
    echo $name
    java -Xss$1g -Xmx$2g -cp "bin":"lib/*" Runner $name $3 > $log_path$name.results  
done

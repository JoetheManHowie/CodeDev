#!/bin/bash
# recommend at least 2g for heap
#run_IM.sh <stack size> <heap size> <beta> <k> <r>

echo Compiling ...

javac -cp "lib/*" -d bin src/*.java

echo Code Successfully Compiled!

graph_path=./graphs/
log_path=./log/
pat1=.sorted

for graph in $graph_path*$pat1
do
    name=$(basename $graph $pat1)
    echo $name
    java -Xss$1g -Xmx$2g -cp "bin":"lib/*" IM_flat $name $3 $4 $5 > $log_path$name.results  
done

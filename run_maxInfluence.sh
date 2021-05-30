#!/bin/bash
# run_maxInfluence.sh <stack> <heap> <beta> <k> <path>

javac -cp "lib/*" -d bin src/*.java

path=$5
pat=.sorted

for graph in ./$path/*$pat
do
    name=$(basename $graph $pat)
    java -Xss$1g -Xmx$2g -cp "bin/":"lib/*" MaxInfluence $path $name $3 $4 
done


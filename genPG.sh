#!/bin/bash

pat1=.obl
web_path=./webgraph/*$pat1
pat2=.edgelist
type='trivalency exponential'
day=$(date +%F)
for graph in $web_path
do
    name=$(basename $graph $pat1)
    echo $name
    java -cp "bin":"lib/*" GraphToEdgelist $name
    
done
echo done first loop
cd graphs/
ls *$pat2
for ext in $type
do
    for graph in ./*$ext$pat2
    do
	name=$(basename $graph \_$ext$pat2)
	echo this $name
    done
done

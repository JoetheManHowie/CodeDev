#!/bin/bash

pat1=.obl
web_path=./webgraph/*$pat1
pat2=.edgelist
type='trivalency exponential'
day=$(date +%F_%T)
for graph in $web_path
do
    name=$(basename $graph $pat1)
    echo $name
    java -cp "bin":"lib/*" GraphToEdgelist $name
done
echo ------- done first loop
cd graphs/
ls *$pat2
for ext in $type
do
    for graph in ./*$ext$pat2
    do
	name=$(basename $graph \_$ext$pat2)
	echo this $name
	saved_name=$name\_$day\_$ext
	echo $saved_name
	sort -k1n -k2n $name\_$ext.edgelist | uniq > $saved_name.sorted
	rm $name\_$ext.edgelist
	java -cp "../lib/*" it.unimi.dsi.webgraph.BVGraph -1 -g ArcListASCIIGraph dummy $saved_name < $saved_name.sorted
	java -cp "../bin":"../lib/*" GenerateLabeledGraphFromTxt $saved_name $saved_name.sorted
    done
done

#!/bin/bash

#genPG.sh <basename> <r>
# run this srcipt from CodeDev
graph=$1
echo Making edgelists

java -cp "bin":"lib/*" GraphToEdgelist $graph
echo ---------------------------- Done.
cd graphs

# add date tag to file
tri=trivalency
exp=exponential
#el=.edgelist
#date_file.sh $graph\_$tri$el
#date_file.sh $graph\_$exp$el

echo Sorting ...

sort -k1n -k2n $graph\_$tri.edgelist | uniq > $graph\_$tri.sorted
sort -k1n -k2n $graph\_$exp.edgelist | uniq > $graph\_$exp.sorted

echo ----------------------------- Done.

echo Making edges of graph

java -cp "../lib/*" it.unimi.dsi.webgraph.BVGraph -1 -g ArcListASCIIGraph dummy $graph\_$tri < $graph\_$tri.sorted
java -cp "../lib/*" it.unimi.dsi.webgraph.BVGraph -1 -g ArcListASCIIGraph dummy $graph\_$exp < $graph\_$exp.sorted

echo Adding the probabilities

java -cp "../bin":"../lib/*" GenerateLabeledGraphFromTxt $graph\_$tri $graph\_$tri.sorted
java -cp "../bin":"../lib/*" GenerateLabeledGraphFromTxt $graph\_$exp $graph\_$exp.sorted

echo Done.

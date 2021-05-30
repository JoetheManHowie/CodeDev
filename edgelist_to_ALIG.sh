#!/bin/bash
# ex:

pat=.edgelist
cd coarsened_graphs/
for graph in ./*$pat
do
    name=$(basename $graph $pat)
    echo $name
    sort -k1n -k2n $name.edgelist | uniq > $name.sorted
    #rm $name$pat
    java -cp "../lib/*" it.unimi.dsi.webgraph.BVGraph -1 -g ArcListASCIIGraph dummy $name < $name.sorted
    java -cp "../bin":"../lib/*" GenerateLabeledGraphFromTxt $name $name.sorted 
done

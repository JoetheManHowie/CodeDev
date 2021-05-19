#!/bin/bash

#getWG.sh <basename>
#(Run this script in Codedev)
www="http://data.law.di.unimi.it/webdata/"
graph=$1

cd webgraph/
echo Downloading graph ... 

wget $www/$graph/$graph.graph
wget $www/$graph/$graph-t.graph
wget $www/$graph//$graph.properties
wget $www/$graph/$graph-t.properties
echo ------------------------------------ DONE.

echo Creating graph ...
nohup java -cp "../lib/*" it.unimi.dsi.webgraph.BVGraph -o -O -L $graph &
echo ------------------------------------ DONE.

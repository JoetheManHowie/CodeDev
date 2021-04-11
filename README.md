Make these directories:
-------------------------

**mkdir graphs/ webgraph/**

How to make webgraphs:
----------------------
In webgraph/ run

**wget <the four files for the webgraph you want (graph and properties, again for the transpose)>**

**nohup java -cp "../lib/*" it.unimi.dsi.webgraph.BVGraph -o -O -L basename&**

In CodeDev/ run

**nohup java -cp "bin":"lib/*" GraphToEdgelist baseame number&**

In graphs/ run

**nohup sort -k1n -k2n basename.edgelist | uniq > basename_sorted.edgelist&**

**nohup java -cp "../lib/\*" it.unimi.dsi.webgraph.BVGraph -1 -g ArcListASCIIGraph dummy basename < basename_sorted.txt&**

**nohup java -cp "../bin":"../lib/\*" GenerateLabeledGraphFromTxt basename basename_sorted.txt&**

How to compile
--

**javac -cp "lib/\*" -d bin src/\*.java**


How to use these structures.
----------------------------------------

Coarsen an ArchLabelledGraph

**nohup java -Xss4g -Xmx64g -cp "bin":"lib/\*" Runner basename integer> basename.out 2>&1&**

##

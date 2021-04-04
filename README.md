How to compile
--

Download the webgraph library from http://webgraph.di.unimi.it. Put the jar files in a directory lib. Compile as follows:

**javac -cp "lib/\*" -d bin src/\*.java**


How to use these structures.
----------------------------------------

Coarsen an ArchLabelledGraph

**java -cp "bin":"lib/\*" Runner graph's basename integer**

##

Make these directories:
-------------------------

**mkdir graphs/ webgraph/**

How to make webgraphs:
----------------------

**wget <the four files for the webgraph you want (graph and properties, again for the transpose)>**

**java -cp "lib/*" it.unimi.dsi.webgraph.BVGraph -o -O -L basename**

**nohup java -cp "bin":"lib/*" graphToEdgelist baseame&**

**sort -k1n -k2n basename.txt | uniq > basename_sorted.txt**

**java -cp "lib/\*" it.unimi.dsi.webgraph.BVGraph -1 -g ArcListASCIIGraph dummy basename < basename_sorted.txt**

**java -cp "bin":"lib/\*" GenerateLabeledGraphFromTxt basename basename_sorted.txt**

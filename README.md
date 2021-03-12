How to compile
--

Download the webgraph library from http://webgraph.di.unimi.it. Put the jar files in a directory lib. Compile as follows:

**javac -cp "lib/\*" -d bin src/\*.java**


How to use these structures.
----------------------------------------

Coarsen an ArchLabelledGraph

**java -cp "bin":"lib/\*" Runner <graph's basename> <integer>**


##

Make a graph
----------

There is a simple python executable in graphs/ that creates a random probability graph using the webgraph frame work

**buildGraph <number of nodes> <number of edges>**

Note: n >= m, otherwise the script breaks. Working on fixing this..

How to compile
--

Download the webgraph library from http://webgraph.di.unimi.it. Put the jar files in a directory lib. Compile as follows:

**javac -cp "lib/\*" -d bin src/\*.java**



How to create an ArcLabeled Webgraph 
--

Suppose we have a graph given as a list of edges and their probabilities (multiplied by some big number, e.g. 1000, so that we store integers instead of floats). E.g. 

(smallexample.txt)

0 1 200

0 2 300

2 1 400

2 3 200

3 1 500 

In order to convert this to Webgraph format we need to first sort the text file by first column then second. We also remove any accidental duplicate. So, if the file is not already sorted and the duplicates removed, we do:

**sort -k1n -k2n smallexample.txt | uniq > smallexample_sorted.txt**


We first create the underlying graph, no labels (weight) yet

**java -cp "lib/\*" it.unimi.dsi.webgraph.BVGraph -1 -g ArcListASCIIGraph dummy smallexample < smallexample.txt**

You will see three files: *smallexample.graph, smallexample.offsets, smallexample.properties*


Now we want to add the labels (probabilities) of the edges. 

**java -cp "bin":"lib/\*" GenerateLabeledGraphFromTxt smallexample smallexample.txt**

You will see some .w files in addition. 



How to use these structures.
----------------------------------------

If you want to print a labeled graph do:

**java -cp "bin":"lib/\*" PrintLabeledGraph smallexample**


##

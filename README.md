# Two main functions:
------------------------

_This repo has java programs to coarsen webgraphs for Influence maximization analysis. The purpose of the summary is to shorten the time that calculating the influence takes._

Java and Shell files:
---------------------

- `getWG.sh` grabs the four webgraph file needed from `http://law.di.unimi.it/datasets.php` given a basename.
- `genPG.sh` which makes the .edgelist file from the webgraph files. Then it also makes the .sorted file and from there creates the ArcLabelledImmutableGraphs webgraphs for each webgraph in the directory webgraph/ 
- `Coarsen.java`, uses `run_coarsen.sh` to execute on a directory of ArcLabelledImmutableGraphs webgraphs.   
-
-
- 

Installation:
--------------
### Make the following folders in the directory.

`mkdir coarsened_graphs/ graphs/ webgraph/ bin/ log/`


Compiling and Running (Shell Scripts):
--------------------------------------


Coarsen Summary:
----------------




Influence Maximization:
-------------------------


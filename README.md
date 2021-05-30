# Two main functions:
------------------------

_This repo has java programs to coarsen webgraphs for Influence maximization analysis. The purpose of the summary is to shorten the time that calculating the influence takes._

Java and Shell files:
---------------------
_Note: for each program an example on how to run the code is given in the top comments so one can easily cat <program> to see how to run the code._


- `getWG.sh` grabs the four webgraph file needed from `http://law.di.unimi.it/datasets.php` given a basename to the webgraph/ directory. Then it builds the .obl files.
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


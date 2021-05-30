# Two main functions:
------------------------

_This repo has java programs to coarsen webgraphs for Influence maximization analysis. The purpose of the summary is to shorten the time that calculating the influence takes._

Java and Shell files:
---------------------
_Note: for each program an example on how to run the code is given in the top comments so one can easily cat <program> to see how to run the code._


- `getWG.sh` grabs the four webgraph file needed from `http://law.di.unimi.it/datasets.php` given a basename to the webgraph/ directory. Then it builds the .obl files.
- `genPG.sh` which makes the .edgelist file from the webgraph files for each webgraph in the webgraph/ directory. Then it also makes the .sorted file and from there creates the ArcLabelledImmutableGraphs webgraphs for each webgraph in the directory webgraph/ 
- `Coarsen.java`, uses `Runner.java` to execute a single graph, and uses `run_coarsen.sh` to execute on a directory of ArcLabelledImmutableGraphs webgraphs. Outputs a .edgelist file of the coarsen graphs edgelist with weights.
- `edgelist_to_ALIG.sh` which converts a .edgelist to all the files needed for an ArcLabelledImmutableGraph. 
- `MaxInfluence.java` takes an ArcLabelledImmutableGraph and finds the k seed nodes to acheive maximum influence. Can run on a whole directory with `run_maxInfluence.sh` given path to directory/


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


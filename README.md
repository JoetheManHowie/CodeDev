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

_Note, `Pair.java` is a helper class for the coarsen algorithm_

Installation:
--------------
### Make the following folders in the directory.

`mkdir coarsened_graphs/ graphs/ webgraph/ bin/ log/`


Compiling and Running (Shell Scripts):
--------------------------------------
Compile command: `javac -cp "lib/*" -d bin src/*.java`

_Note: The compilecommand is included at the beginning of each shell script._

_Note: The .edgelist files are files of text with the directed edges and their probability from 1 to 1000. Furthermore, .sorted is the sorted vrsion of .edgelist. These convensions are assumed in the shell scripts, other extensions are ignored._



1) To get webgraphs, you can use `./getWG.sh <basename>` and download the four files from `http://law.di.unimi.it/datasets.php` and makes the .obl file.

2) From the .obl, you can use `./genPG.sh` to generate the edgelists (with weight distributions tri and exp) for each webgraph in the directory webgraph/. This script will also generate the .sorted, .graph, .offset, .properties, and the .w files as well which are the files associated ArcLabelledImmutableGraph files.

3) To get a coarsened summary based on SCC, you can run `./run_coarsen.sh <stack size> <heap size> <r> <path/>` which will make the _summary.edgelist file for each ArcLabelledImmutableGraph in `<path/>`. The output is saved to a .results file in the log/ directory.

4) To make an ArcLabelledImmutableGraph from a .edgelist file, you can run `edgelist_to_ALIG.sh <path/>` which will convert all the .edgelist files in `<path/>` to the seven ArcLabelledImmutableGraph files.

5) Finally, to generate the Influence Maximization, you can run `run_maxInfluence.sh <stack> <heap> <beta> <k> <path/>` which will find the `<k>` seed nodes for each ArcLabelledImmutableGraph in the `<path/>` and prints the information to a .im file in the log/ directory.
 
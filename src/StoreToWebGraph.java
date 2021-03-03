/*
 * Joe Howie, Mar 1 2021
 * copied from email from Mahdi
 */

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.IncrementalImmutableSequentialGraph;


public class StoreToWebGraph{
    public static void main(String [] args) throws Exception {
	int [] arr = new int[]{1, 2, 3, 4, 5, 7}; // adj list
	//create new webgraph
	final IncrementalImmutableSequentialGraph g = new IncrementalImmutableSequentialGraph();
	ExecutorService executor = Executors.newSingleThreadExecutor();
	final Future<Void> future = executor.submit(new Callable<Void>(){
		public Void call() throws IOException {
		    BVGraph.store(g, "twohop"); // twohop is the basename for the file on disk where you store the new graph
		    return null;
		}
	    });
	// in your program, you should put this line in a loop to save ALL neighbour lists
	g.add(arr, 0, arr.length);


	// At the very end, once you have added all the lists you want
	// call this last add
	g.add(IncrementalImmutableSequentialGraph.END_OF_GRAPH);
	future.get();
	executor.shutdown();
    }
    
}

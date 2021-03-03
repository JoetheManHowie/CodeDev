/*
 * Joe Howie, Feb 10 2021
 * Implementation of corasen graph alg 
 */
//java classes
//package prob_sum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;
import java.util.Iterator;
import java.util.LinkedList;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

//custom classes
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.IncrementalImmutableSequentialGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.labelling.Label;


/**
 * Class that takes a probability graph G and finds a coarsened graph G' 
 * This is done using Algorithm 1 from the "Coarsen Massive influence Networks 
 * For Scalable Diffusion analysis"
 *
 */
public class Coarsen {
    int r; // number of samples of graph G
    ArcLabelledImmutableGraph G; // probabilistic graph G
    int n; // Number of vertices
    final IncrementalImmutableSequentialGraph finalUniverse;
    final IncrementalImmutableSequentialGraph finalUniverse_t;
    //private static LinkedList<Integer> finalUniverse[]; //Adjacency List
    //private static LinkedList<Integer> finalUniverse_t[]; //Adjacency List
    
    /**
     * System.out.println is way too much to type
     * So I made this.
     * @param Generic s (whatever you may want to print)
     */
    public static <S> void print(S s){
	System.out.println(s);
    }
    
    /**
     * Initialized the corasenGraph object
     * @params String of the graph files base name and the number of worlds
     */
    public Coarsen(String basename, int num_of_worlds) throws Exception{
	r = num_of_worlds;
	print("Loading Graph...");
	G = ArcLabelledImmutableGraph.load(basename+".w");
	print("Graph Loaded");
	print("");
	n = G.numNodes();
	print("The loaded graph has "+n+" vertices.");
	//Initialize graphs
	finalUniverse = new IncrementalImmutableSequentialGraph();
	finalUniverse_t = new IncrementalImmutableSequentialGraph();
	
	print("Create the final universe lists");
	createFinalUniverse(basename);
	print("Make the transpose");
	getTranspose();
	print("print the SCCs");
	//printSCCs();
	
    }
    /**
     * Builds the connected component graph g_r for 
     */
    public void createFinalUniverse(String basename) throws Exception{
	print("set up graph stuff");
	ExecutorService executor = Executors.newSingleThreadExecutor();
	final Future<Void> future = executor.submit(new Callable<Void>(){
		public Void call() throws IOException {
		    BVGraph.store(G, "FU_"+basename);
		    return null;
		}
	    });
	print("Initialize LinkedList Object");
	LinkedList <Integer> edges = new LinkedList<Integer>();
	print("Initialize Random Object");
	Random rand = new Random();
	// EACH NODE
	print("Loop over edges");
	for (int v = 0; v<n; v++){
	    int [] v_neighbours = G.successorArray(v);
	    Label[] v_labels = G.labelArray(v);
	    int v_degs = G.outdegree(v);
	    print("loop over neighbours");
	    // EACH EDGE
	    for (int i= 0; i<v_degs; i++){
		int u = v_neighbours[i];
		Label label = v_labels[i];
		int count = 0;
		int w = (int)label.getLong();
		print("Edge "+u+", "+v+" weight: "+w);
		// EACH UNIVERSE
		for (int j = 0; j<r; j++){
		    int test = rand.nextInt(1000);
		    print("test: "+test);
		    if(test <=w)
			count++;
		    else
			break;
		}
		if(count==r)
		    print("edge added");
		    edges.add(u);
	    }
	    int [] arr = new int[edges.size()];
	    int a_count = 0;
	    for(Integer a: edges){
		arr[a_count] = a;
		a_count++;
	    }
	    finalUniverse.add(arr, 0, arr.length);
	    edges.clear();
	}
	finalUniverse.add(IncrementalImmutableSequentialGraph.END_OF_GRAPH);
	future.get();
	executor.shutdown();
	
    }

    public void getTranspose() throws Exception{
	int nodes = finalUniverse.numNodes();
	print(nodes);
	for (int v = 0; v < nodes; v++){
	    //Iterator<Integer> i = finalUniverse[v].listIterator();
	    // while (i.hasNext())
	    //   finalUniverse_t[i.next()].add(v);
	    int[] v_neighbours = finalUniverse.successorArray(v);
	    int v_degs = finalUniverse.outdegree(v);
	    for (int i = 0; i < v_degs; i++){
		int u = v_neighbours[i];
	    }
	}
    }

    public static void main(String [] args) throws Exception {
	long startTime = System.currentTimeMillis();
	if(args.length == 0) {
	    print("the if happened");
	    args = new String[]{"fang"};
	}
	String basename = args[0];
	int number_of_world = Integer.parseInt(args[1]);
	Coarsen C = new Coarsen(basename, number_of_world);
	print("Total time elapsed = "+(System.currentTimeMillis()-startTime)/1000.0 +" seconds");
    }
}

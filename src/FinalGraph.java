/*
 * Joe Howie, Feb 10 2021
 * Implementation of corasen graph alg 
 */
//java classes

import java.lang.Integer;
import java.util.HashSet;
import java.util.HashMap;
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
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.LazyIntIterator;

/**
 * Class that takes a probability graph G and finds a coarsened graph G' 
 * This is done using Algorithm 1 from the "Coarsen Massive influence Networks 
 * For Scalable Diffusion analysis"
 */
public class FinalGraph {
    int r; // number of samples of graph G
    ArcLabelledImmutableGraph G; // probabilistic graph G
    int n; // Number of vertices
    ImmutableGraph finalUniverse;
    //ImmutableGraph finalUniverse_t;
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
    public FinalGraph(String basename, int num_of_worlds) throws Exception{	
	r = num_of_worlds;
	print("Loading Graph...");
	G = ArcLabelledImmutableGraph.load("graphs/"+basename+".w");
	print("Graph Loaded");
	print("");
	n = G.numNodes();
	print("The loaded graph has "+n+" vertices.");
	
	print("Create the final universe lists");
	createFinalUniverse(basename);
	print("Create final universe transpose");
	getTranspose(basename);	
    }
    /**
     * Builds the connected component graph g_r for 
     * @params String basename, for reading the graph G (probability graph)
     */
    public void createFinalUniverse(String basename) throws Exception{
	//print("set up graph stuff");
	final IncrementalImmutableSequentialGraph gg = new IncrementalImmutableSequentialGraph();
	ExecutorService executor = Executors.newSingleThreadExecutor();
	final Future<Void> future = executor.submit(new Callable<Void>(){
		public Void call() throws IOException {
		    BVGraph.store(gg, "graphs/FU_"+basename);
		    return null;
		}
	    });
	
	Random rand = new Random();
	// EACH NODE
	print("Loop over edges");
	//print("The degree of node 100 is: " + G.outdegree(100));
	for (int v = 0; v<n; v++){
	    ArrayList<Integer> edges = new ArrayList<Integer>();
	    //print("Current node : "+v);
	    int [] v_neighbours = G.successorArray(v);
	    Label[] v_labels = G.labelArray(v);
	    int v_degs = G.outdegree(v);
	    // EACH EDGE
	    for (int i= 0; i<v_degs; i++){
		int u = v_neighbours[i];
		Label label = v_labels[i];
		int count = 0;
		int w = (int)label.getLong();
		//print("Edge "+v+", "+u+" weight: "+w);
		// EACH UNIVERSE
		for (int j = 0; j<r; j++){
		    int test = rand.nextInt(1000);
		    // print("test: "+test);
		    if(test <=w)
			count++;
		    else
			break;
		}
		if(count==r)
		    edges.add(u);
	    }
	    int [] arr = new int[edges.size()];
	    int a_count = 0;
	    for(Integer a: edges){
		arr[a_count] = a;
		a_count++;
	    }
	    gg.add(arr, 0, arr.length);
	}
	print("Done!");
	// stuff to close
	gg.add(IncrementalImmutableSequentialGraph.END_OF_GRAPH);
	future.get();
	executor.shutdown();
	finalUniverse = ImmutableGraph.loadMapped("graphs/FU_"+basename);
	print("the number of nodes of final is: " + finalUniverse.numNodes() + " Number of edges is: " + finalUniverse.numArcs());
    }
        
    /**
     * Calculates the transpose graph. NB: this may not be very space efficient
     * @params String basename, for making the final universe transpose
     */
    public void getTranspose(String basename) throws Exception{
	// temp storage for adj list, while we pull out the transpose adj lists
	@SuppressWarnings("unchecked")
	    LinkedList<Integer> [] adj_list = new LinkedList[n];
	for (int a = 0; a< n; a++)
	    adj_list[a] = new LinkedList<Integer>();
	// iterate over finalUniverse the first time! so now numNodes can be called and what have you
	for (int v = 0; v<n; v++){
	    int [] v_neighbours = finalUniverse.successorArray(v);
	    int v_degs = finalUniverse.outdegree(v);
	    for (int i = 0; i<v_degs; i++){
		int u = v_neighbours[i];
		//print(v+", "+u);
		adj_list[u].add(v);

	    }

	}
	/*
	NodeIterator iter = finalUniverse.nodeIterator();
	while (iter.hasNext()){
	    int v = iter.nextInt();
	    LazyIntIterator v_neighbours = iter.successors();
	    int u = v_neighbours.nextInt();
	    print(v+", "+u);
	    while(u !=-1){
	
		// add v to u's adj list
		adj_list[u].add(v);
		
		u = v_neighbours.nextInt();
	    }
	}
	*/
	// stuff for making a graph using WbGraph framework
	final IncrementalImmutableSequentialGraph gg = new IncrementalImmutableSequentialGraph();
	ExecutorService executor = Executors.newSingleThreadExecutor();
	final Future<Void> future = executor.submit(new Callable<Void>(){
		public Void call() throws IOException {
		    BVGraph.store(gg, "graphs/FUt_"+basename);
		    return null;
		}
	    });
	// add all the adj list to transpose graph
	for(LinkedList<Integer> a: adj_list){
	    int [] dumdum = new int[a.size()];
	    // convert linkedlist to arr
	    for (int i = 0; i<a.size(); i++)
		dumdum[i] = a.get(i);
	    Arrays.sort(dumdum);
	    gg.add(dumdum, 0, dumdum.length);
	}
	// stuff to close 
	gg.add(IncrementalImmutableSequentialGraph.END_OF_GRAPH);
	future.get();
	executor.shutdown();
    }
    /**
     * Main Method

    public static void main(String [] args) throws Exception {
	long startTime = System.currentTimeMillis();
	if(args.length == 0) {
	    print("the if happened");
	    args = new String[]{"fang"};
	}
	String basename = args[0];
	int number_of_world = Integer.parseInt(args[1]);
	FinalGraph C = new FinalGraph(basename, number_of_world);
	print("Total time elapsed = "+(System.currentTimeMillis()-startTime)/1000.0 +" seconds");
    }
    */
}

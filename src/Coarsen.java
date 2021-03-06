/*
 * Joe Howie, Feb 10 2021
 * Implementation of corasen graph alg 
 */
//java classes
//package prob_sum;

import java.lang.Integer;
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
	getTranspose(basename);
	print("Run over final Universe transpose for the street cred");
	runOver(finalUniverse_t);
	print("Now find the SCC's");
	getSCCs();
	//print("print the SCCs");
	//printSCCs();
	
    }
    /**
     * Builds the connected component graph g_r for 
     * @params String basename, for reading the graph G (probability graph)
     */
    public void createFinalUniverse(String basename) throws Exception{
	//print("set up graph stuff");
	ExecutorService executor = Executors.newSingleThreadExecutor();
	final Future<Void> future = executor.submit(new Callable<Void>(){
		public Void call() throws IOException {
		    BVGraph.store(G, "FU_"+basename);
		    return null;
		}
	    });
	//print("Initialize LinkedList Object");
	LinkedList <Integer> edges;
	//print("Initialize Random Object");
	Random rand = new Random();
	// EACH NODE
	print("Loop over edges");
	for (int v = 0; v<n; v++){
	    edges = new LinkedList<Integer>();
	    print("Current node : "+v);
	    int [] v_neighbours = G.successorArray(v);
	    Label[] v_labels = G.labelArray(v);
	    int v_degs = G.outdegree(v);
	    // EACH EDGE
	    for (int i= 0; i<v_degs; i++){
		int u = v_neighbours[i];
		Label label = v_labels[i];
		int count = 0;
		int w = (int)label.getLong();
		print("Edge "+v+", "+u+" weight: "+w);
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
		    edges.add(u);
	    }
	    int [] arr = new int[edges.size()];
	    int a_count = 0;
	    print("here");
	    // it lags out here on node 100 ... idk why right now, still thinking
	    for(Integer a: edges){
		print(a);
		arr[a_count] = a;
		a_count++;
	    }
	    finalUniverse.add(arr, 0, arr.length);
	    //edges.clear();
	    print("Successfully added edge list!");
	}
	print("Done!");
	// stuff to close
	finalUniverse.add(IncrementalImmutableSequentialGraph.END_OF_GRAPH);
	future.get();
	executor.shutdown();
	
    }
    /**
     * Loops over a graph for the first time
     * @params IncrementalImmutableSequentialGraph G, (final universe transpose)
     */
    public void runOver(IncrementalImmutableSequentialGraph G) throws Exception{
	NodeIterator iter = G.nodeIterator();
	while (iter.hasNext()){
	    int v = iter.nextInt();
	    LazyIntIterator v_neighbours = iter.successors();
	    int u = v_neighbours.nextInt();
	    while(u !=-1){
		print(v+", "+u);
		u = v_neighbours.nextInt();
	    }
	}
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
	NodeIterator iter = finalUniverse.nodeIterator();
	while (iter.hasNext()){
	    int v = iter.nextInt();
	    LazyIntIterator v_neighbours = iter.successors();
	    int u = v_neighbours.nextInt();
	    while(u !=-1){
		// add v to u's adj list
		adj_list[u].add(v);
		print(v+", "+u);
		u = v_neighbours.nextInt();
	    }
	}
	// stuff for making a graph using WbGraph framework
	ExecutorService executor = Executors.newSingleThreadExecutor();
	final Future<Void> future = executor.submit(new Callable<Void>(){
		public Void call() throws IOException {
		    BVGraph.store(G, "FUt_"+basename);
		    return null;
		}
	    });
	// add all the adj list to transpose graph
	for(LinkedList<Integer> a: adj_list){
	    int [] dumdum = new int[a.size()];
	    // convert linkedlist to arr
	    for (int i = 0; i<a.size(); i++)
		dumdum[i] = a.get(i);
	    finalUniverse_t.add(dumdum, 0, dumdum.length);
	}
	// stuff to close 
	finalUniverse_t.add(IncrementalImmutableSequentialGraph.END_OF_GRAPH);
	future.get();
	executor.shutdown();
    }
    /**
     * do a graph traversal to find the SCCs
     */
    public void getSCCs(){
	int num_cc = 0;
	Stack<Integer> stack = new Stack<Integer>();
	// mark nodes as not visited for first run
	boolean [] visited = new boolean[n];
	for (int i = 0; i<n; i++)
	    visited[i] = false;

	// fill nodes in stack
	for (int i = 0; i< n; i++)
	    if (visited[i]==false)
		fillOrder(i, visited, stack);

	// second run, clean visited
	for (int i = 0; i<n; i++)
	    visited[i] = false;

	// iterator over nodes aligned in the stack
	// and mark as nodes are visited
	while (stack.empty() == false){
	    int v = (int)stack.pop();
	    if (visited[v] == false){
		num_cc++;
		DFS(v, visited);
	    }
	}
	print("Number of SCC's: "+num_cc);
    }
    /**
     * Traverses the final Universe Graph 
     * @params int v (the node we are visiting now); bool arr of nodes visited; Stack of nodes to be visited
     */
    public void fillOrder(int v, boolean [] visited, Stack<Integer> stack){
	visited[v] = true;
	int [] v_neighbours = finalUniverse.successorArray(v);
	int v_degs = finalUniverse.outdegree(v);
	for (int i = 0; i<v_degs; i++){
	    int u = v_neighbours[i];
	    if(visited[u] == false)
		fillOrder(u, visited, stack);
	}
	stack.push(Integer.valueOf(v));
    }
    /**
     * @params node v, bool arr of visited nodes
     */
    public void DFS(int v, boolean [] visited){
	visited[v] = true;
	int [] v_neighbours = finalUniverse_t.successorArray(v);
	int v_degs = finalUniverse_t.outdegree(v);
	for (int i = 0; i<v_degs; i++){
	    int u = v_neighbours[i];
	    if(visited[u] == false)
		DFS(u, visited);
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

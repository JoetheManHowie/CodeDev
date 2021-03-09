 /*
 * Joe Howie, Feb 10 2021
 * Implementation of corasen graph alg 
 */
//java classes
//package prob_sum;

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
 *
 */
public class Coarsen {
    int r; // number of samples of graph G
    ArcLabelledImmutableGraph G; // probabilistic graph G
    int n; // Number of vertices
    ImmutableGraph finalUniverse;
    ImmutableGraph finalUniverse_t;
    ImmutableGraph F; // this is F
    HashMap<Integer, Integer> pi; // this is function pi
    HashMap<Integer, ArrayList<Integer>> SCC; // W --> set of keys; and w --> .size of each value
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
	
	print("Create the final universe lists");
	createFinalUniverse(basename);

	print("Make the transpose");
	getTranspose(basename);
	print("Run over final Universe transpose for the street cred");
	//runOver(finalUniverse_t);
	print("Now find the SCC's");
	getSCCs();
	print(SCC.get(3017).size());
	// --- Build W, F, pi, and w
	// --> The keys of SCC is the set W
	// --> the pi is pi. 
	// --> the .size of each value in SCC is w
	
	// --> F ?--------?
	print("Make the set F");
	makeF(basename);
	
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
		    BVGraph.store(gg, "FU_"+basename);
		    return null;
		}
	    });
	
	Random rand = new Random();
	// EACH NODE
	print("Loop over edges");
	print("The degree of node 100 is: " + G.outdegree(100));
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
		// print("Edge "+v+", "+u+" weight: "+w);
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
	finalUniverse = ImmutableGraph.loadMapped("FU_");
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
	NodeIterator iter = finalUniverse.nodeIterator();
	while (iter.hasNext()){
	    int v = iter.nextInt();
	    LazyIntIterator v_neighbours = iter.successors();
	    int u = v_neighbours.nextInt();
	    while(u !=-1){
		// add v to u's adj list
		adj_list[u].add(v);
		// print(v+", "+u);
		u = v_neighbours.nextInt();
	    }
	}
	// stuff for making a graph using WbGraph framework
	final IncrementalImmutableSequentialGraph gg = new IncrementalImmutableSequentialGraph();
	ExecutorService executor = Executors.newSingleThreadExecutor();
	final Future<Void> future = executor.submit(new Callable<Void>(){
		public Void call() throws IOException {
		    BVGraph.store(gg, "FUt_"+basename);
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
	finalUniverse_t = ImmutableGraph.loadMapped("FUt_");
	print("the number of nodes of transpose is: " + finalUniverse_t.numNodes() + " Number of edges is: " + finalUniverse_t.numArcs());
    }
    /**
     * do a graph traversal to find the SCCs
     */
    public void getSCCs(){
	SCC = new HashMap<Integer, ArrayList<Integer>>();
	pi =  new HashMap<Integer, Integer>();
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
		DFS(v, visited, num_cc);
		num_cc++; // next CC

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
    public void DFS(int v, boolean [] visited, int num_cc){
	visited[v] = true;
	// add node to this cc
	pi.put(Integer.valueOf(v), Integer.valueOf(num_cc));
	ArrayList<Integer> curr_list = SCC.get(Integer.valueOf(num_cc));
	if (curr_list == null)
	    curr_list = new ArrayList<Integer>();
	// print(SCC.get(Integer.valueOf(num_cc)));
	curr_list.add(v);
	// print(num_cc);
	SCC.put(Integer.valueOf(num_cc), curr_list);
	int [] v_neighbours = finalUniverse_t.successorArray(v);
	int v_degs = finalUniverse_t.outdegree(v);
	for (int i = 0; i<v_degs; i++){
	    int u = v_neighbours[i];
	    if(visited[u] == false)
		DFS(u, visited, num_cc);
	}	
    }
    /**
     * Creates F, which is a set of edges between clusters
     */
    public void makeF(String basename) throws Exception{
	final IncrementalImmutableSequentialGraph gg = new IncrementalImmutableSequentialGraph();
	ExecutorService executor = Executors.newSingleThreadExecutor();
	final Future<Void> future = executor.submit(new Callable<Void>(){
		public Void call() throws IOException {
		    BVGraph.store(gg, "F_set_"+basename);
		    return null;
		}
	    });
	int m = SCC.size();
	for (int cx = 0; cx<m; cx++){
	    print("cx is " +cx);
	    ArrayList<Integer> nodes = SCC.get(cx);
	    //print(nodes);
	    ArrayList<Integer> edges = new ArrayList<Integer>();
	    for (Integer v: nodes){
		int [] v_neighbours = finalUniverse.successorArray(v);
		int v_degs = finalUniverse.outdegree(v);
		//print(v_degs);
		for (int i = 0; i < v_degs; i++){
		    int u = v_neighbours[i];
      		    int cy = pi.get(u);
		    // print("cy is "+cy);
		    if (cx != cy)
			edges.add(cy);
		}
		
	    }
	    //print(edges);
	    int [] arr = new int[edges.size()];
	    
	    int a_count = 0;
	    //print("here");
	    for(Integer a: edges){
		//print(a);
		arr[a_count] = a;
		a_count++;
	    }
	    //print(arr);
	    print("add edge list "+cx);
	    Arrays.sort(arr);
	    //for (int i = 0; i < arr.length; i++)
	    //	print(arr[i]);
	    gg.add(arr, 0, arr.length);
	    print("added");
	}
	print("Done!");
	gg.add(IncrementalImmutableSequentialGraph.END_OF_GRAPH);
	future.get();
	executor.shutdown();
	F = ImmutableGraph.loadMapped("F_set_");
	print("the number of nodes of transpose is: " + F.numNodes() + " Number of edges is: " + F.numArcs());
    }
    
    /**
     * Main Method
     */
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

/*
 * Joe Howie, Mar 26 2021
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
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

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
    int Gn; // Number of vertices
    int n;
    ImmutableGraph finalUniverse;
    ImmutableGraph finalUniverse_t;
    HashMap<ArrayList<Integer>, Integer> q;
    HashMap<Integer, HashSet<Integer>> F;
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
	G = ArcLabelledImmutableGraph.load("graphs/"+basename+".w");
	print("Graph Loaded");
	Gn = G.numNodes();
	print("The loaded graph has "+Gn+" vertices.");
	print("Create the final universe lists");
	createFinalUniverse(basename);
	finalUniverse = ImmutableGraph.loadMapped("graphs/"+basename+"_FU");
	print("the number of nodes of final is: " + finalUniverse.numNodes() + " Number of edges is: " + finalUniverse.numArcs());
	print("Create final universe transpose");
	getTranspose(basename);	
	//r = num_of_worlds;
	finalUniverse_t = ImmutableGraph.loadMapped("graphs/"+basename+"_FUt");
	//G = ArcLabelledImmutableGraph.load(basename+".w");
	//print("Graph Loaded");
	//print("");
	n = finalUniverse.numNodes();
	print("Now find the SCC's");
	getSCCs();
	//print(SCC.get(193));
	// --- Build W, F, pi, and w
	// --> The keys of SCC is the set W
	// --> the pi is pi. 
	// --> the .size of each value in SCC is w
	// --> F = make a graph of c_i's
	print("Make the set F and q");
	//makeF(basename);
	makeF();
	print("Making q array");
	refine_q();

    }
    public void createFinalUniverse(String basename) throws Exception{
	final IncrementalImmutableSequentialGraph gg = new IncrementalImmutableSequentialGraph();
	ExecutorService executor = Executors.newSingleThreadExecutor();
	final Future<Void> future = executor.submit(new Callable<Void>(){
		public Void call() throws IOException {
		    BVGraph.store(gg, "graphs/"+basename+"_FU");
		    return null;
		}
	    });
	
	Random rand = new Random();
	// EACH NODE
	for (int v = 0; v<Gn; v++){
	    ArrayList<Integer> edges = new ArrayList<Integer>();
	    int [] v_neighbours = G.successorArray(v);
	    Label[] v_labels = G.labelArray(v);
	    int v_degs = G.outdegree(v);
	    // EACH EDGE
	    for (int i= 0; i<v_degs; i++){
		int u = v_neighbours[i];
		Label label = v_labels[i];
		int count = 0;
		int w = (int)label.getLong();
		// EACH UNIVERSE
		for (int j = 0; j<r; j++){
		    int test = rand.nextInt(1000);
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
	//finalUniverse = ImmutableGraph.loadMapped("graphs/FU_"+basename);
    }
    /**
     * Calculates the transpose graph. NB: this may not be very space efficient
     * @params String basename, for making the final universe transpose
     */
    public void getTranspose(String basename) throws Exception{
	// temp storage for adj list, while we pull out the transpose adj lists
	@SuppressWarnings("unchecked")
	    LinkedList<Integer> [] adj_list = new LinkedList[Gn];
	for (int a = 0; a< Gn; a++)
	    adj_list[a] = new LinkedList<Integer>();
	// iterate over finalUniverse the first time! so now numNodes can be called and what have you
	for (int v = 0; v<Gn; v++){
	    int [] v_neighbours = finalUniverse.successorArray(v);
	    int v_degs = finalUniverse.outdegree(v);
	    for (int i = 0; i<v_degs; i++){
		int u = v_neighbours[i];
		adj_list[u].add(v);
	    }
	}
	final IncrementalImmutableSequentialGraph gg = new IncrementalImmutableSequentialGraph();
	ExecutorService executor = Executors.newSingleThreadExecutor();
	final Future<Void> future = executor.submit(new Callable<Void>(){
		public Void call() throws IOException {
		    BVGraph.store(gg, "graphs/"+basename+"_FUt");
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
     * 
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
	curr_list.add(v);
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
    public void makeF() throws Exception{
	F = new HashMap<Integer, HashSet<Integer>>();
	q = new HashMap<ArrayList<Integer>, Integer>();
	int m = SCC.size();
	int edge_num = 0;
	for (int cx = 0; cx<m; cx++){
	    ArrayList<Integer> nodes = SCC.get(cx);
	    HashSet<Integer> edges = new HashSet<Integer>();
	    for (Integer v: nodes){
		int [] v_neighbours = finalUniverse.successorArray(v);
		int v_degs = finalUniverse.outdegree(v);
		for (int i = 0; i < v_degs; i++){
		    int u = v_neighbours[i];
      		    int cy = pi.get(u);
		    if (cx != cy){
			edges.add(cy);
			ArrayList<Integer> key = new ArrayList<Integer>();
			key.add(cx);
			key.add(cy);
			q.put(key, 1);
		    }
		}	
	    }
	    F.put(Integer.valueOf(cx), edges);
	    edge_num += edges.size();
	}
	print("Done!");
	print("the number of coarsened nodes is: " + F.size() + " Number of edges is: " + edge_num);
    }
    /**
     * @params string basename
     */
    public void refine_q() throws Exception{
	for(int v = 0; v<Gn; v++){
	    int[] v_neighbours = G.successorArray(v);
	    Label[] v_labels = G.labelArray(v);
	    int v_degs = G.outdegree(v);
	    for (int i = 0; i<v_degs; i++){
		int u = v_neighbours[i];
		Label label = v_labels[i];
		int w = (int)label.getLong();
		int pi_v = pi.get(v);
		int pi_u = pi.get(u);
		if(F.get(pi_v).contains(pi_u)){ 
		    ArrayList<Integer> key = new ArrayList<Integer>();
		    key.add(pi_v);
		    key.add(pi_u);
		    q.put(key, q.get(key) * (1000 - w)); 
		}
	    }
	}
	//11
	int a = SCC.size();
	for(int cx = 0; cx < a; cx++){
	    HashSet<Integer> edges = F.get(cx);
	    for(Integer cy: edges){
		ArrayList<Integer> key = new ArrayList<Integer>();
		key.add(cx);
		key.add(cy);
		q.put(key, 1-q.get(key));
	    }
	}
    }
}

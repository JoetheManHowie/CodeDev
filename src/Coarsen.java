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
    int n;
    ImmutableGraph finalUniverse;
    ImmutableGraph finalUniverse_t;
    HashMap<ArrayList<Integer>, Integer> q;
    HashMap<Integer, ArrayList<Integer>> F;
    //ImmutableGraph F; // this is F
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
    public Coarsen(String basename) throws Exception{
	//r = num_of_worlds;
	print("Loading Graph...");
	finalUniverse = ImmutableGraph.loadMapped("graphs/FU_"+basename);
	finalUniverse_t = ImmutableGraph.loadMapped("graphs/FUt_"+basename);
	//G = ArcLabelledImmutableGraph.load(basename+".w");
	print("Graph Loaded");
	print("");
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
	refine_q(basename);

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
    public void makeF() throws Exception{
	F = new HashMap<Integer, ArrayList<Integer>>();
	q = new HashMap<ArrayList<Integer>, Integer>();
	/*
	IncrementalImmutableSequentialGraph gg = new IncrementalImmutableSequentialGraph();
	ExecutorService executor = Executors.newSingleThreadExecutor();
	Future<Void> future = executor.submit(new Callable<Void>(){
		public Void call() throws IOException {
		    BVGraph.store(gg, "graphs/F_set_"+basename);
		    return null;
		}
	    });
	*/
	int m = SCC.size();
	int edge_num = 0;
	for (int cx = 0; cx<m; cx++){
	    //print("cx is " +cx);
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
	    /*
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
	    //print("add "+cx+" edge list of size "+arr.length);
	    Arrays.sort(arr);
	    
	    //for (int i = 0; i < arr.length; i++)
	    //	print(arr[i]);
	    gg.add(arr, 0, arr.length);
	    //print("added");
	    */
	}
	print("Done!");
	print("the number of coarsened nodes is: " + F.size() + " Number of edges is: " + edge_num);

	/*
	gg.add(IncrementalImmutableSequentialGraph.END_OF_GRAPH);
	future.get();
	executor.shutdown();
	F = ImmutableGraph.loadMapped("graphs/F_set_"+basename);
	print("the number of coarsened nodes is: " + F.numNodes() + " Number of edges is: " + F.numArcs());
	*/
    }
    public void refine_q(String basename) throws Exception{
	ArcLabelledImmutableGraph G = ArcLabelledImmutableGraph.load("graphs/"+basename+".w");
	int nodes = G.numNodes();
	for(int v = 0; v<nodes; v++){
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
		    //print("u: "+v+" v: "+u);
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
	    ArrayList<Integer> edges = F.get(cx);
	    for(Integer cy: edges){
		ArrayList<Integer> key = new ArrayList<Integer>();
		key.add(cx);
		key.add(cy);
		q.put(key, 1-q.get(key));
	    }
	}
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
	Coarsen C = new Coarsen(basename);
	print("Total time elapsed = "+(System.currentTimeMillis()-startTime)/1000.0 +" seconds");
    }
    */
}

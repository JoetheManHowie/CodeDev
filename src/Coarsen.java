
/*
 * Joe Howie May 12th 2021
 */
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.IncrementalImmutableSequentialGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.labelling.Label;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.LazyIntIterator;
// Outer class

/**
 * Corasen is the main class here. It contains several inner classes 
 * which help construct the coarsened graph from the probabistic graph
 */
public class Coarsen{
    String ext = "_temp";
    ArcLabelledImmutableGraph PG;
    int nodes;
    String basename;
    int r;
    InstanceGraph P_i;
    SCC pie;
    VWIG H; //def from 4.1 (VWIG: Vertex Weighted Influence Graph)
    // print like python & matlab
    public static <S> void print(S s){
	System.out.println(s);
    }
    public Coarsen(String basename, int r) throws Exception{
	this.basename = basename;
	this.r = r;
	this.PG = ArcLabelledImmutableGraph.load("graphs/"+basename+".w");
	this.nodes = PG.numNodes();
	makeCoarse(); // after this, we have FU (the final intersection of all the sa), and its SCC
	print(pie.pie[4]);
	H = new VWIG(pie);
	H.see_bag();
    }
    public void makeCoarse() throws Exception{
	Random rand = new Random();
	SCC curr = new SCC();
	pie = new SCC();
	for (int q = 0; q<r; q++){
	    print("Graph : "+q);
	    final IncrementalImmutableSequentialGraph gg = new IncrementalImmutableSequentialGraph();
	    ExecutorService executor = Executors.newSingleThreadExecutor();
	    final Future<Void> future = executor.submit(new Callable<Void>(){
		    public Void call() throws IOException {
			BVGraph.store(gg, "graphs/"+basename+ext);
			return null;
		    }
		});

	    for (int v = 0; v<nodes; v++){
		ArrayList<Integer> edges = new ArrayList<Integer>();
		int [] v_neighbours = PG.successorArray(v);
		Label[] v_labels = PG.labelArray(v);
		int v_degs = PG.outdegree(v);
		for (int i = 0; i<v_degs; i++){
		    int u = v_neighbours[i];
		    Label label = v_labels[i];
		    int weight = (int)label.getLong();
		    if (rand.nextInt(1000) <= weight){
			print(v+" "+u);
			edges.add(u);
		    }
		}
		int [] arr = new int[edges.size()];
		int count = 0;
		for (Integer a: edges){
		    arr[count] = a;
		    count++;
		 }
		gg.add(arr, 0, arr.length);
	    }
	    gg.add(IncrementalImmutableSequentialGraph.END_OF_GRAPH);
	    future.get();
	    executor.shutdown();
	    P_i = new InstanceGraph();
	    curr = new SCC(P_i);
	    print("New sampled connected components");
	    curr.printSCC();	    
	    pie.meet(curr);
	    print("After the meet");
	    pie.printSCC();
	}
    }
    // inner class 1
    /**
     * This class holds the graph and transpose graph for a sampling of PG
     */
    public class InstanceGraph{
	ImmutableGraph graph;
	ImmutableGraph graphT;
	public InstanceGraph() throws Exception{
	    this.graph = ImmutableGraph.loadMapped("graphs/"+basename+ext);
	    getTrans();
	}
	public void getTrans() throws Exception{
	    @SuppressWarnings("unchecked")
		LinkedList<Integer> [] adj_list = new LinkedList[nodes];
	    for (int a = 0; a< nodes; a++)
		adj_list[a] = new LinkedList<Integer>();
	    for (int v = 0; v<nodes; v++){
		int [] v_neighbours = graph.successorArray(v);
		int v_degs = graph.outdegree(v);
		for (int i = 0; i<v_degs; i++){
		    int u = v_neighbours[i];
		    adj_list[u].add(v);
		}
	    }
	    final IncrementalImmutableSequentialGraph gg = new IncrementalImmutableSequentialGraph();
	    ExecutorService executor = Executors.newSingleThreadExecutor();
	    final Future<Void> future = executor.submit(new Callable<Void>(){
		    public Void call() throws IOException {
			BVGraph.store(gg, "graphs/"+basename+ext+"_t");
			return null;
		    }
		});
	    for(LinkedList<Integer> a: adj_list){
		int [] dumdum = new int[a.size()];
		for (int i = 0; i<a.size(); i++)
		    dumdum[i] = a.get(i);
		Arrays.sort(dumdum);
		gg.add(dumdum, 0, dumdum.length);
	    }
	    gg.add(IncrementalImmutableSequentialGraph.END_OF_GRAPH);
	    future.get();
	    executor.shutdown();
	    this.graphT = ImmutableGraph.loadMapped("graphs/"+basename+ext+"_t");
	}
    }
    // inner class 2
    /**
     * This class finds the SCC of an instant graph
     */
    public class SCC{
	int num_scc;
	int [] pie;
	InstanceGraph graph;
	private Stack<Integer> stack;
	private boolean[] visited;
	public SCC(InstanceGraph graph) throws Exception{
	    pie = new int[nodes];
	    this.graph = graph;
	    findSCC();
	}
	public SCC() throws Exception{
	    pie = new int[nodes];
	    blankSCC();
	}
	private void blankSCC() throws Exception{
	    for (int i = 0; i < nodes; i++)
		pie[i] = 0;
	}
	public void printSCC()throws Exception{
	    for(int i = 0; i<nodes; i++)
		print("Node : " + i +"\t"+pie[i]);
	}
	public void meet(SCC other) throws Exception{
	    HashMap<Pair, Integer> map = new HashMap<Pair, Integer>();
	    int ll = 0;
	    int [] M = new int[nodes];
	    
	    for (int i = 0; i < nodes; i++){
		Pair pair  = new Pair(this.pie[i], other.pie[i]);
		//print(pair.i+" "+pair.j);
		//print(map.containsKey(pair));
		if (!map.containsKey(pair)){
		    //print(pair.i+" "+pair.j);
		    map.put(pair, ll);
		    ll++;
		}
		M[i] = map.get(pair);
		//print("M "+M[i]);
	    }
	    int temp = 0;
	    for (int i = 0; i<nodes; i++){
		if (M[i] > temp)
		    temp = M[i];
		pie[i] = M[i];
	    }
	    num_scc = temp;
	}
	public void findSCC() throws Exception{
	    num_scc = 0;
	    stack = new Stack<Integer>();
	    visited = new boolean[nodes];
	    set_visit_to_false();
	    for (int i = 0; i<nodes; i++)
		if (visited[i] == false)
		    fillOrder(i);
	    set_visit_to_false();
	    while (stack.empty() == false){
		int v = (int)stack.pop();
		if (visited[v] == false){
		    DFS(v);
		    num_scc++;
		}
	    }
	    print("Number of SCC: "+num_scc);
	    
	}
	private void set_visit_to_false(){
	    for(int i = 0; i<nodes; i++)
		visited[i] = false;
	}
	private void DFS(int v){
	    visited[v] = true;
	    pie[v] = num_scc;
	    int [] v_neighbours = graph.graphT.successorArray(v); 
	    int v_degs = graph.graphT.outdegree(v);
	    for (int i = 0; i<v_degs; i++){
		int u = v_neighbours[i];
		if (visited[u] == false)
		    DFS(u);
	    }
	}
	private void fillOrder(int v){
	    visited[v] = true;
	    int [] v_neighbours = graph.graph.successorArray(v);
	    int v_degs = graph.graph.outdegree(v);
	    for (int i = 0; i<v_degs; i++){
		int u = v_neighbours[i];
		if(visited[u] == false)
		    fillOrder(u);
	    }
	    stack.push(Integer.valueOf(v));
	}
    }
    /**
     * for hash keys, I dislike the Integer class, but here we are..you may get a deprecated warning its fine.
     */
    public class Pair{
	private Integer i;
	private Integer j;
	public Pair(int i, int j){
	    this.i = new Integer(Integer.valueOf(i));
	    this.j = new Integer(Integer.valueOf(j));
	}
	// src: https://bit.ly/3eK1P1B
	@Override
	public int hashCode(){
	    return i.hashCode() ^ j.hashCode();
	}
	@Override
	public boolean equals(Object obj){
	    return (obj instanceof Pair ) && ((Pair) obj).i.equals(i) && ((Pair) obj).j.equals(j);
	}
    }
    public class VWIG {
	SCC scc;
	HashMap<Integer, HashSet<Integer>> bag;
	HashMap<Pair, Integer> q;
	HashMap<Integer, HashSet<Integer>> F;
	int [] w; // W would then be the indicies of w[]
	
	public VWIG(SCC scc) throws Exception{
	    this.scc = scc;
	    w = new int[scc.num_scc+1];
	    build_w();
	    build_bag();
	    build_qF();
	}
	private void build_w() throws Exception{
	    // build w & W
	    scc.printSCC();
	    print(scc.num_scc);
	    for (int i = 0; i<nodes; i++)
		w[scc.pie[i]]++;
	}
	private void build_bag() throws Exception{
	    bag = new HashMap<Integer, HashSet<Integer>>();
	    HashSet<Integer> set;
	    for (int v = 0; v<nodes; v++){
		Integer C = Integer.valueOf(scc.pie[v]);
		print(v+": "+C);
		set = bag.get(C);
		set.add(v);
		bag.put(C, set);
	    }
	}
	public void see_bag() throws Exception{
	    HashSet<Integer> arr;
	    for (int c = 0; c < scc.num_scc; c++){
		arr = bag.get(c);
		print("Super node: "+c);
		for (Integer a: arr)
		    System.out.print(a+" ");
		
	    }
	}
	private void build_qF() throws Exception{
	    F = new HashMap<Integer, HashSet<Integer>>();
	    q = new HashMap<Pair, Integer>();
	    int m = scc.num_scc;
	    int edge_num = 0;
	    for (int cx = 0; cx<m; cx++){
		//Pair nodes = scc;
		
	    }
	}
    }
}

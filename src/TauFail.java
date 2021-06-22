/*
 * Joe Howie June 22nd 2021
 */
import java.text.DecimalFormat; 
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
public class TauFail{
    String ext = "_t";
    ArcLabelledImmutableGraph PG;
    ImmutableGraph GT;
    int nodes;
    int edges;
    String basename;
    int tau;

    //    GraphAndTrans P_i;
    SCC pie;
    VWIG H; //def from 4.1 (VWIG: Vertex Weighted Influence Graph)
    // print like python & matlab
    public static <S> void print(S s){
	System.out.println(s);
    }
    public TauFail(String basename, int tau) throws Exception{
	this.basename = basename;
	this.tau = tau;
	this.PG = ArcLabelledImmutableGraph.load("graphs/"+basename+".w");
	this.edges = (int)PG.numArcs();
	this.nodes = PG.numNodes();

	makeCoarse(); // after this, we have FU (the final intersection of all the sa), and its SCC
	GT = ImmutableGraph.loadMapped("graphs/"+basename+ext);
	long tim = System.currentTimeMillis();
	pie = new SCC();
	print("Time to get SCC "+(System.currentTimeMillis() - tim )/1000.0+" seconds");
	print("----------------------------");
	
	long time = System.currentTimeMillis();
	H = new VWIG(pie);
	print("Time to make the coarsened data structure "+(System.currentTimeMillis() - tim)/1000.0+ " seconds");
	print("----------------------------");
	save_to_file();

    }
    public int getOutDegree(Integer u){
	HashSet<Integer> set = H.F.get(u);
	if (set == null)
	    return 0;
	else
	    return set.size();
    }
    public void save_to_file(){
	try {
	    FileWriter writer = new FileWriter("alg2_graphs/"+basename+"_alg2sum.edgelist");
	    for(Pair pt: H.q.keySet()){
		writer.write(pt.i+"\t"+pt.j+"\t"+H.q.get(pt)+"\n");
	    }
	    writer.close();
	}
	catch (IOException e){
	    e.printStackTrace();
	}
    }
    public double getVertexRatio() throws Exception{
	double ws = H.W_size();
	return ws/this.nodes*100;
    }
    public double getEdgeRatio() throws Exception{
	double fs = H.F_size();
	return fs/this.edges*100;
    }
    public void makeCoarse() throws Exception{
	Random rand = new Random();
	long time = System.currentTimeMillis();
	@SuppressWarnings("unchecked")
	    LinkedList<Integer> [] adj_list = new LinkedList[nodes];
	for (int a = 0; a< nodes; a++)
	    adj_list[a] = new LinkedList<Integer>();	
	for (int v = 0; v<nodes; v++){
	    ArrayList<Integer> edges = new ArrayList<Integer>();
	    int [] v_neighbours = PG.successorArray(v);
	    Label [] v_labels = PG.labelArray(v);
	    int v_degs = PG.outdegree(v);
	    int counter = 0;
	    int count = 0;
	    int i = 0;
	    HashSet<Integer> skip = new HashSet<Integer>(); 
	    while ( ((count < tau) || counter < (v_degs * tau))){
		//for (int i = 0; i<v_degs; i++){
		if (i >= v_degs) break;
		int u = v_neighbours[i];
		if (skip.contains(u)) break; // stops inf loops
		Label label = v_labels[i];
		int weight = (int)label.getLong();
		int roll = rand.nextInt(1000);
		
		if (weight < roll){
		    adj_list[u].add(v);
		    skip.add(u);
		    count = 0;
		}
		else
		    count++;
		i = (i+1)%v_degs;
		counter++;
	    }
	}
	print("done");
	final IncrementalImmutableSequentialGraph gg = new IncrementalImmutableSequentialGraph();
	ExecutorService executor = Executors.newSingleThreadExecutor();
	final Future<Void> future = executor.submit(new Callable<Void>(){
		public Void call() throws IOException {
		    BVGraph.store(gg, "graphs/"+basename+ext);
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
	print("----------------------------");
	print("Time to make Immutable graph "+(System.currentTimeMillis() - time)/1000.0+" seconds");
	print("----------------------------");
    }
    /**
     * This class finds the SCC of an instant graph
     */      
    public class SCC{
	int num_scc;
	int [] pie;
	
	private Stack<Integer> stack;
	private boolean[] visited;
	public SCC() throws Exception{
	    pie = new int[nodes];
	    findSCC();
	}
	private void blankSCC() throws Exception{
	    for (int i = 0; i < nodes; i++)
		pie[i] = 0;
	}
	public void printSCC()throws Exception{
	    for(int i = 0; i<nodes; i++)
		print("Node : " + i +"\t"+pie[i]);
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
	}
	private void set_visit_to_false(){
	    for(int i = 0; i<nodes; i++)
		visited[i] = false;
	}
	private void DFS(int v){
	    visited[v] = true;
	    pie[v] = num_scc;
	    int [] v_neighbours = GT.successorArray(v); 
	    int v_degs = GT.outdegree(v);
	    for (int i = 0; i<v_degs; i++){
		int u = v_neighbours[i];
		if (visited[u] == false)
		    DFS(u);
	    }
	}
	private void fillOrder(int v){
	    visited[v] = true;
	    int [] v_neighbours = PG.successorArray(v);
	    int v_degs = PG.outdegree(v);
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
	    bag = new HashMap<Integer, HashSet<Integer>>();
	    build_bag();
	    F = new HashMap<Integer, HashSet<Integer>>();
	    q = new HashMap<Pair, Integer>();
	    build_qF();
	    refine_q();
	}
	private void build_w() throws Exception{
	    // build w & W
	    //scc.printSCC();
	    //print("build w");
	    for (int i = 0; i<nodes; i++)
		w[scc.pie[i]]++;
	}
	public int F_size() throws Exception{
	    return q.size(); // q has prob for each edge in F so same same
	}
	public int W_size() throws Exception{
	    return w.length; 
	}
	private void build_bag() throws Exception{
	    for (int v = 0; v<nodes; v++){
		Integer C = Integer.valueOf(scc.pie[v]);
		HashSet<Integer> set = new HashSet<Integer>();
		if (bag.get(C) != null)
		    set = bag.get(C);
		set.add(Integer.valueOf(v));
		bag.put(C, set);
	    }
	}
	public void see_bag() throws Exception{
	    HashSet<Integer> arr;
	    for (int c = 0; c < scc.num_scc+1; c++){
		arr = bag.get(Integer.valueOf(c));
		System.out.print("\nSuper node "+c+": ");
		for (Integer a: arr)
		    System.out.print(a+" ");
	    }
	    print("");
	}
	private void build_qF() throws Exception{
	    int m = scc.num_scc;
	    int edge_num = 0;
	    for (int cx = 0; cx<m; cx++){
		HashSet<Integer> nodes = bag.get(cx);
		HashSet<Integer> edges = new HashSet<Integer>();
		for (Integer v: nodes){
		    int [] v_neighbours = PG.successorArray(v);
		    int v_degs = PG.outdegree(v);
		    for (int i = 0; i<v_degs; i++){
			int u = v_neighbours[i];
			int cy = scc.pie[u];
			if (cx != cy){
			    edges.add(cy);
			    Pair key = new Pair(cx, cy);
			    q.put(key, 1000);
			}
		    }
		}
		F.put(Integer.valueOf(cx), edges);
		edge_num += edges.size();
	    }
	    //print("The number of coarsened nodes is: " + F.size()+1 + " Number of edges is: " + edge_num);
	}
	public void print_F(){
	    //HashMap<Integer, HashSet<Integer>>
	    print("F");
	    for(Integer a: F.keySet()){
		System.out.print(a+": ");
		for(Integer b: F.get(a)){
		    System.out.print(b+" ");
		}
		print("");
	    }
	}
	public void print_q(){
	    //HashMap<Pair, Integer>
	    print("q");
	    //print(q.keySet());
	    for(Pair pt: q.keySet()){
		print(pt.i+" "+pt.j+": "+q.get(pt));
	    }
	}
	private void refine_q() throws Exception{
	    //print("Refine q");
	    for (int v = 0; v<nodes; v++){
		int [] v_neighbours = PG.successorArray(v);
		Label [] v_labels = PG.labelArray(v);
		int v_degs = PG.outdegree(v);
		for (int i = 0; i<v_degs; i++){
		    int u = v_neighbours[i];
		    //print(v+" "+u);
		    Label label = v_labels[i];
		    int weight = (int)label.getLong();
		    int pi_v = scc.pie[v];
		    int pi_u = scc.pie[u];
		    //print(pi_v+" "+pi_u);
		    //print(F.get(pi_v));
		    if (F.get(pi_v) == null)
		    	continue;
		    if (F.get(pi_v).contains(pi_u)){
			Pair key = new Pair(pi_v, pi_u);
			//print(q.get(key));
			q.put(key, q.get(key) * (1000 - weight)/1000);
		    }
		}
	    }
	    for (int cx = 0; cx<scc.num_scc; cx++){
		HashSet<Integer> edges = F.get(cx);
		for (Integer cy: edges){
		    Pair key = new Pair(cx, cy);
		    q.put(key, 1000 - q.get(key));
		}
	    }
	}
    }
    public static void main(String [] args) throws Exception{
	long time = System.currentTimeMillis();
	if (args.length < 2){
	    print("give a basename and tau");
	    System.exit(1);
	}
	String basename = args[0];
	int tau = Integer.parseInt(args[1]);
	print("Running: "+basename);
	TauFail a2 = new TauFail(basename, tau);
	DecimalFormat dec = new DecimalFormat("#.###");
	print("Results for "+basename);
	print("|V| = "+ dec.format(a2.nodes)+", |E| = "+ dec.format(a2.edges));
	print("|F| = " + dec.format(a2.H.F_size()) + ", |F|/|E| = " + dec.format(a2.getEdgeRatio()) + ", |W| = "+ dec.format(a2.H.W_size()) + ", |W|/|V| = " + dec.format(a2.getVertexRatio()));
	print("----------------------------");
	print("Time Elapsed = "+(System.currentTimeMillis() - time)/1000.0+ " seconds");
	
    }
}

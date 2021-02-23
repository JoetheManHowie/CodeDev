/*
 * Joe Howie, Feb 10 2021
 * Implementation of corasen graph alg 
 */
//java classes
import java.util.Arrays;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.lang.Math;
//custom classes
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.Label;

public class coarsenGraph {
    /**
     * System.out.println is way too much to type
     * So I made this.
     * @param Generic s (whatever you may want to print)
     */
    public static <S> void print(S s){
	System.out.println(s);
    }
    /**
     * Standard BFS, customed to my evil purposes
     * @params v: the start node; n: the # of nodes
     * @return int [] of order nodes taversed
     */
    public static LinkedList<List<Integer>> BFS(ArcLabelledImmutableGraph G){
	print("Start BFS sampling");
	int n = G.numNodes();
	//int m = G.numEdges();
	boolean[] visited = new boolean[n]; // number of nodes in the []
	Queue<Integer> Q = new LinkedList<Integer>(); // add the generic type
	int pos = 0;
	LinkedList<List<Integer>> edge = new LinkedList<List<Integer>>(); 
	Q.add(pos);
	visited[pos] = true;
	
	//cnt++;
	while (Q.size() > 0){
	    int u = Q.poll();
	    int [] u_neighbours = G.successorArray(u);
	    Label[] u_labels = G.labelArray(u);
	    int u_deg = G.outdegree(u);
	    for (int i = 0; i < u_deg; i++){
		pos = u_neighbours[i];
		if (visited[pos] == false){
		    print("Nieghbour:"+pos);
		    visited[pos] = true;
		    Q.add(pos);
		    //sample edge - (u,v)
		    Label label = u_labels[i];
		    int test = (int)Math.random()*(1000 - 1);
		    print("Test: "+test);
		    if (test < label.getLong()){
			List<Integer> node = new LinkedList<Integer>(Arrays.asList(u, pos));
			edge.add(node);
		    } 
		}
	    }
	}
	print("Done BFS");
	return edge;
    }
    /**
     * Loads the ArcLabelledImmutableGraph graph and returns it.
     * @params String of the graph files base name 
     * @return the ArcLabelledImmutableGraph graph g
     */
    public static ArcLabelledImmutableGraph LoadGraph(String basename) throws Exception{
	print("loading Graph...");
	ArcLabelledImmutableGraph G = ArcLabelledImmutableGraph.load(basename+".w");
	print("Graph Loaded");
	print("");
	return G;
    }
    
    
    public static int[][] EdgeList(ArcLabelledImmutableGraph G){
	int[][] el = new int[(int)G.numArcs()][2];
	int n = G.numNodes();
	int count = 0;
	for(int v = 0; v < n; v++){
	    int [] v_neighbours = G.successorArray(v);
	    Label[] v_labels = G.labelArray(v);
	    int v_degs = G.outdegree(v);

	    for(int i = 0; i < v_degs; i++){
		int u = v_neighbours[i];
		Label label = v_labels[i];
		double test = Math.random()*(1000.0 - 1);
		print("Test: "+test);
		print(v + "\t" + u + "\t" + label.getLong());
		if(test < label.getLong()){
		    print()
		}
		count++;
	    }
	}
	return el;
    }
    
    /**
     * The algorithm from the 2017 paper on graph coraseing
     * (linear implementation)
     * @params ArcLabelledImmutableGraph G and int r
     * @return vertex weighted influence graph H and a mapping pi: V-->W
     */
    public static void Alg1(ArcLabelledImmutableGraph G, int r) {
	// STAGE ONE -- (L: 1-5) Create partition of a r-robust SCC w.r.t. r random graphs.
	//1. set P_0 = set of vertices
	int n = G.numNodes();
	int [] P = new int[n];
	for(int i = 0; i<n; i++){ P[i] = i; }
	// ---- Does WebGraph have a better way to grab all the nodes?
	
	for(int i = 0; i<r; i++){ //2. DONE
	    //3. make a random sampled graph from G, callit g_i
	    // need edge list

	    //LinkedList<List<Integer>> g_i = BFS(G);

	    // -- construct G using edgelist method (mod to it returns a graph instead of int[] tree)
	    // -- use the prob weights and a rand gen to determine if (u,v) in g_i
	    
	    //4. compute a partition C_i w/ all SCC's in g_i
	    // -- not sure where in the paper they formaly define C -- Def 4.9 doesn't tell me much
	    
	    //5. P_i = P_(i-1) (^) C_i
	}
	//6. Build W, F, pi, and w from G and P_r according to Def 4.1 (p.4)
	
	// STAGE TWO -- (L: 6-12) Construct coarsened influence graph H.
	//7. q[c_x,c_y] = 1, forall (c_x, c_y) in F
	//for()
    }
    /** 
     * Main Method
     */
    public static void main(String [] args) throws Exception {
	if(args.length == 0) {
	    print("the if happened");
	    args = new String[]{"fang"};
	}
	ArcLabelledImmutableGraph G = LoadGraph(args[0]); // open the graph -- might not be most efficient way...
	//long[][] el = EdgeList(G);
	int r = 4; // # of SCC graphs
	//Alg1(G, r);
        LinkedList<List<Integer>> subG = BFS(G);
	for(List<Integer> node: subG){
	    for(Integer u: node){
		print(u);
	    }
	    print("next edge");
	}
    }
}


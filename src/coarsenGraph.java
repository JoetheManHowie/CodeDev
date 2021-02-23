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

class Edge{
    int val1;
    int val2;
    public Edge(int val1, int val2){
	this.val1 = val1;
	this.val2 = val2;
    }
    public void print_vals(){
	System.out.println(this.val1+" "+this.val2);
    }
}

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
    /**
     * Finds the edges in the intersection of r graphs 
     * @returns edge list
     */
    public static LinkedList<Edge> EdgeList(ArcLabelledImmutableGraph G, int r){
	LinkedList<Edge> el = new LinkedList<Edge>();
	int n = G.numNodes();
	int count = 0;
	for(int v = 0; v < n; v++){
	    int [] v_neighbours = G.successorArray(v);
	    Label[] v_labels = G.labelArray(v);
	    int v_degs = G.outdegree(v);

	    for(int i = 0; i < v_degs; i++){
		int u = v_neighbours[i];
		Label label = v_labels[i];
		// r rolls, keeps max
	        double max_roll = Math.random()*(1000.0 - 1); 
		for (int j = 0; j<r; j++){
		    double test = Math.random()*(1000.0 - 1);
		    //print(test);
		    if(test>max_roll) max_roll = test;
		}	
		print("Max roll: "+max_roll);
		if(max_roll < label.getLong()){
		    print(max_roll);
		    Edge e = new Edge(v, u);
		    el.add(e);
		    count++;
		    print("Added edge: "+v + "\t" + u + "\t" + label.getLong());
		}
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
	//for(int i = 0; i<n; i++){ P[i] = i; }
	// ---- Does WebGraph have a better way to grab all the nodes?
	
	//3. make a random sampled graph from G, callit g_i

	// el is the edge list of the edges that survived the r random rolls
	LinkedList<Edge> el = EdgeList(G, r); // effectivly line three
	// $$ For 2-5 loop over edges once, make r random rolls
	
	for(Edge e: el){
	    e.print_vals();
	}
	//4. compute a partition C_i w/ all SCC's in g_i
	
	
	
	//5. P_i = P_(i-1) (^) C_i
	
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
	Alg1(G, r);
	/*
	LinkedList<List<Integer>> subG = BFS(G);
	for(List<Integer> node: subG){
	    for(Integer u: node){
		print(u);
	    }
	    print("next edge");
	}
	*/
    }
}


/**
     * Standard BFS, customed to my evil purposes
     * @params v: the start node; n: the # of nodes
     * @return int [] of order nodes taversed
     */
/*
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
*/

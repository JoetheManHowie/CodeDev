/* This implementation of Reversed Influence Sampling algorithm
 * (Borgs et al., Maximizing social influence in nearly optimal time) 
 * was written by Diana Popova, University of Victoria, Canada. 
 * Data structure for the hypergraph is flat, one-dimensional, arrays.
 *
 * Adaptation: May 20th 2021, Joe Howie 
 *
 */
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Random;
import java.util.Arrays;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.BVGraph; 
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.Label;

public class IM_flat {
    ArcLabelledImmutableGraph G;
    Coarsen C;
    int n, k;
    long m;
    String basename;
    double  beta;
    int nMAX = 175000000; // maximum possible for 16 GB main memory --- divided by 8 to make 2GB
    BitSet marked, sk_gone, nodes_gone;
    int[] sketches;
    int[] nodes;
    int[] node_infl;
    
    int count_sketches; // the length of sketches and nodes arrays
    
    public static <S> void print(S s){
	System.out.println(s);
    }
    public IM_flat(String basename,  Double beta, int k) throws Exception {
	this.G = ArcLabelledImmutableGraph.load("graphs/"+basename+".w");
	this.n = G.numNodes();
	this.m = G.numArcs();
        this.basename = basename;
        this.beta = beta;
        this.k = k;
        print("beta = " + beta);
        print("k = " + k);
	
        print("n="+n + ", m=" +m  + ", R=" +( beta * k * m*Math.log(n)  ));
	
	marked = new BitSet(n);
        
        sketches = new int[nMAX];
        nodes = new int[nMAX];
        node_infl = new int[n];
        
        for(int i=0;i<nMAX;i++){
	    sketches[i] = -1;
	    nodes[i] = -1;
        }
    	get_sketch();
    }
    private void get_sketch() {
	double R = beta * k * m * Math.log(n);
	double weight_of_current_index = 0.0;
        int sketch_num = 0;
	long startTime = System.currentTimeMillis();
        count_sketches = 0;
        Random gen_rnd = new Random();
	while(weight_of_current_index < R){
	    int v = gen_rnd.nextInt(n);
	    marked.clear();
	    BFS(v,marked);
	    int total_out_degree = 0;
            int iteration = 0;
	    for (int u = marked.nextSetBit(0); u >= 0; u = marked.nextSetBit(u+1)){
		sketches[count_sketches + iteration] = sketch_num;
                nodes[count_sketches + iteration]  = u;
                node_infl[u] = node_infl[u] + 1;
                iteration = iteration + 1;
		total_out_degree = total_out_degree + G.outdegree(u);
	    }
	    weight_of_current_index = weight_of_current_index + total_out_degree;
	    sketch_num = sketch_num + 1;
            count_sketches = count_sketches + marked.cardinality();
	}
	print("");
	print("Number of Sketches: " + sketch_num);
	print("");
	System.gc();
	long startSeeds = System.currentTimeMillis();
	double set_infl = 0.0;
        double coeff = 1.0 * n/sketch_num;
        sk_gone = new BitSet(sketch_num);
        nodes_gone = new BitSet(count_sketches);
        get_seeds(sketches, nodes, node_infl, k, count_sketches, sketch_num, set_infl, coeff, sk_gone, nodes_gone);
	double getSeeds = (System.currentTimeMillis() - startSeeds)/1000.0;
	print("");
        print("Calculating seeds took " + getSeeds + " seconds");
    }
    private void BFS(int v, BitSet marked) {
	Random random = new Random();
	Deque<Integer> queue = new ArrayDeque<Integer>();
	queue.add(v);
        marked.set(v);
	while (!queue.isEmpty()) {
            int u = queue.remove();
            int[] u_neighbors = G.successorArray(u);
	    Label [] label = G.labelArray(u);
            int u_deg = G.outdegree(u);
	    for (int ni = 0; ni < u_deg; ni++) {
                int uu = u_neighbors[ni];
		int weight = (int)label[ni].getLong();
                double xi = random.nextDouble();
		if (!marked.get(uu) && xi < weight) {
                    queue.add(uu);
                    marked.set(uu);
                }
            }
        }
    }
    private void get_seeds(int[] iSketch,int[] iNode, int[] node_infl, int k_left, int count_sketches, int sketch_num, double set_infl, double coeff, BitSet sk_gone, BitSet nodes_gone) {
	// Calculating the node with max influence
        int infl_max = 0;
        int max_node = 0;
	for(int v=0;v<n;v++){
	    if(node_infl[v] > infl_max){
		infl_max = node_infl[v];
		max_node = v;
	    }
        }
	set_infl = set_infl + infl_max * coeff;
	print(max_node +", Its Influence = " + infl_max);
	// Stopping condition: no need to re-calculate the influence, if we already got the k seeds
        if((k_left - 1)==0) {
            print("Set Influence = " + set_infl);
            return;
        }
	// Re-calculating the influence of the remaining nodes: remove max node and the sketches it participated in
        // plus re-calculate the influence
        node_infl[max_node] = 0;
	for(int j=0;j<count_sketches;j++){
	    if(nodes_gone.get(j))
		continue;
	    else{
                if((iNode[j] == max_node) && (!sk_gone.get(iSketch[j]))){
		    int redundant_sketch = sketches[j];
                    sk_gone.set(redundant_sketch);
		    // As sketches are added to the array in numerical order, the same redundant sketch can be found before and after the max node
                    if(j < (count_sketches - 1)){
                        int l = j+1;
                        while((l<count_sketches) && (iSketch[l] == redundant_sketch)){
                            if(node_infl[iNode[l]] > 0)
                                node_infl[iNode[l]] = node_infl[iNode[l]] - 1;
			    nodes_gone.set(nodes[l]);
                            l++;
                        }
                    }
		    if(j>0){
			int ll = j-1;
			while(((ll+1) > 0) && (iSketch[ll] == redundant_sketch)){
                            if(node_infl[iNode[ll]] > 0)
                                node_infl[iNode[ll]] = node_infl[iNode[ll]] - 1;
			    nodes_gone.set(nodes[ll]);
                            ll--;
                        }
                    }
                }
            }
        }
	nodes_gone.set(max_node);
	get_seeds(iSketch, iNode, node_infl, k_left-1, count_sketches, sketch_num, set_infl, coeff, sk_gone, nodes_gone);
    }
    
    
    public static void main(String[] args) throws Exception {
	long startTime = System.currentTimeMillis();
	long estimatedTime;
	
        if(args.length < 3) {
            print("Specify: basename, beta, k");
            System.exit(1);
        }
        String basename  = args[0];
	double beta = Double.valueOf(args[1]);
        int k = Integer.valueOf(args[2]);
	//for (int i = 0; i<args.length;i++)
	//    print(args[i]);
        IM_flat imfl = new IM_flat(basename, beta, k);
	estimatedTime = System.currentTimeMillis() - startTime;
	print("Time elapsed = " + estimatedTime /(1000.0) + " sec");
    }
}

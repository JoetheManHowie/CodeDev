/* This implementation of Reversed Influence Sampling algorithm
 * (Borgs et al., Maximizing social influence in nearly optimal time) 
 * was written by Diana Popova, University of Victoria, Canada. 
 * Data structure for the hypergraph is flat, one-dimensional, arrays.
 *
 * Adaptation: May 20th 2021, Joe Howie 
 *
 */
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.BitSet;
import java.util.Deque;
import java.util.Random;
import java.util.Arrays;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.BVGraph; 
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.Label;

public class IM_flat {
    Coarsen C;
    int n, k;
    long m;
    //String basename;
    double  beta;
    int nMAX = 175000000; // maximum possible for 16 GB main memory --- divided by 8 to make 2GB
    BitSet marked, sk_gone, nodes_gone;
    int[] sketches;
    int[] nodes;
    int[] node_infl;
    int[] coarse_node_infl;
    int[] coarse_infl;
    int[] infl;
    int count_sketches; // the length of sketches and nodes arrays
    /**
     */
    public static <S> void print(S s){
	System.out.println(s);
    }
    /**
     */
    public IM_flat(String basename, Double beta, int k, int r) throws Exception {
	long time = System.currentTimeMillis();
	this.C = new Coarsen(basename, r);
	print("Time to coarsen graph "+(System.currentTimeMillis()-time)/1000.0+ " seconds");
	//this.G = ArcLabelledImmutableGraph.load("graphs/"+basename+".w");
	this.n = C.nodes;
	this.m = C.edges;
        //this.basename = basename;
        this.beta = beta;
        this.k = k;
        print("beta = " + beta);
        print("k = " + k);
	time = System.currentTimeMillis();
	marked = new BitSet(n);
	node_infl = new int[n];
	infl = new int[k];
	coarse_infl = new int[k];
	set_node_sketch();
	print("Running influence maximization on original graph");
    	get_sketch();
	print("Time to run IM on original graph "+(System.currentTimeMillis()-time)/1000.0+ " seconds\n");
	time = System.currentTimeMillis();
	set_node_sketch();
	coarse_node_infl = new int[C.H.W_size()];
	marked = new BitSet(C.H.W_size());
	print("Running influence maximization on coarsened graph");
	coarse_sketch();
	print("Time to run IM on coarsened graph "+(System.currentTimeMillis()-time)/1000.0+ " seconds\n");
	compare_og_to_coarse();
    }
    /**
     * 
     */
    public void compare_og_to_coarse(){
	// holds the label of the coarsened node
	// for the node which has the same index
	// in infl array ie the node infl[i] is in where_the_infl[i]
	int [] where_the_infl = new int[k]; 
	for (int i = 0; i < infl.length; i++){
	    where_the_infl[i] = C.pie.pie[infl[i]];
	    print("Influencer "+infl[i]+" is in cluster "+where_the_infl[i]);
	}
	for (int i = 0; i < where_the_infl.length; i++)
	    for (int j = 0; j < coarse_infl.length; j++)
		if(where_the_infl[i]==coarse_infl[j])
		    print("Number " +i+ " influencer "+infl[i]+" lives in cluster "+coarse_infl[j]+", the number "+j+" cluster influencer");
			
    }
    /**
     */
    private void set_node_sketch(){
	sketches = new int[nMAX];
        nodes = new int[nMAX];
        
        for (int i = 0; i<nMAX; i++){
	    sketches[i] = -1;
	    nodes[i] = -1;
        }
    }
    private void get_sketch() {
	double R = beta * k * m * Math.log(n);
	print(R);
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
		total_out_degree = total_out_degree + C.PG.outdegree(u);
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
        get_seeds(sketches, nodes, node_infl, k, count_sketches, sketch_num, set_infl, coeff, sk_gone, nodes_gone, n, infl);
	double getSeeds = (System.currentTimeMillis() - startSeeds)/1000.0;
	print("");
        print("Calculating seeds took " + getSeeds + " seconds");
    }
    /**
     */
    private void BFS(int v, BitSet marked) {
	Random random = new Random();
	Deque<Integer> queue = new ArrayDeque<Integer>();
	queue.add(v);
        marked.set(v);
	while (!queue.isEmpty()) {
            int u = queue.remove();
            int[] u_neighbors = C.PG.successorArray(u);
	    Label [] label = C.PG.labelArray(u);
            int u_deg = C.PG.outdegree(u);
	    for (int ni = 0; ni < u_deg; ni++) {
                int uu = u_neighbors[ni];
		int weight = (int)label[ni].getLong();
                int xi = random.nextInt(1000);
		if (!marked.get(uu) && xi <= weight) {
                    queue.add(uu);
                    marked.set(uu);
                }
            }
        }
    }
    /**
     */
    private void coarse_sketch() throws Exception{
	int c_n = C.H.W_size();
	int c_m = C.H.F_size();
	double R = beta * k * c_m * Math.log(c_n);
	print("R = "+R);
	Double weight_of_current_index = 0.0;
        int sketch_num = 0;
	long startTime = System.currentTimeMillis();
        count_sketches = 0;
        Random gen_rnd = new Random();
	while(weight_of_current_index < R){
	    int v = gen_rnd.nextInt(c_n);
	    marked.clear();
	    coarse_BFS(v,marked);
	    int total_out_degree = 0;
            int iteration = 0;
	    for (int u = marked.nextSetBit(0); u >= 0; u = marked.nextSetBit(u+1)){
		sketches[count_sketches + iteration] = sketch_num;
                nodes[count_sketches + iteration]  = u;
                coarse_node_infl[u] = coarse_node_infl[u] + 1;
                iteration = iteration + 1;
		total_out_degree = total_out_degree + C.getOutDegree(u);
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
        double coeff = 1.0 * c_n/sketch_num;
        sk_gone = new BitSet(sketch_num);
        nodes_gone = new BitSet(count_sketches);
        get_seeds(sketches, nodes, coarse_node_infl, k, count_sketches, sketch_num, set_infl, coeff, sk_gone, nodes_gone, c_n, coarse_infl);
	double getSeeds = (System.currentTimeMillis() - startSeeds)/1000.0;
	print("");
        print("Calculating seeds took " + getSeeds + " seconds");
    }
    /**
     */
    private void coarse_BFS(int v, BitSet marked) {
	Random random = new Random();
	Deque<Integer> queue = new ArrayDeque<Integer>();
	queue.add(v);
        marked.set(v);	
	while (!queue.isEmpty()) {
            int u = queue.remove();
	    HashSet<Integer> u_neighbours = C.H.F.get(Integer.valueOf(u));
	    if (u_neighbours == null) continue;
	    for(Integer uu: u_neighbours){
		Pair getme = new Pair(u, uu);
		int weight = C.H.q.get(getme);
                int xi = random.nextInt(1000);
		if (!marked.get(uu) && xi <= weight) {
                    queue.add(uu);
                    marked.set(uu);
                }
            }
        }
    }
    /**
     */
    private void get_seeds(int[] iSketch,
			   int[] iNode,
			   int[] node_infl,
			   int k_left,
			   int count_sketches,
			   int sketch_num,
			   double set_infl,
			   double coeff,
			   BitSet sk_gone,
			   BitSet nodes_gone,
			   int num_nodes,
			   int [] tops) {
	// Calculating the node with max influence
        int infl_max = 0;
        int max_node = 0;
	for (int v = 0; v<num_nodes;v++){
	    if(node_infl[v] > infl_max){
		infl_max = node_infl[v];
		max_node = v;
	    }
        }
	tops[k-k_left] = max_node;
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
	get_seeds(iSketch, iNode, node_infl, k_left-1, count_sketches, sketch_num, set_infl, coeff, sk_gone, nodes_gone, num_nodes, tops);
    }    
    /**
     */
    public static void main(String[] args) throws Exception {
	long startTime = System.currentTimeMillis();
	long estimatedTime;
        if(args.length < 4) {
            print("Specify: basename, beta, k, r");
            System.exit(1);
        }
        String basename  = args[0];
	double beta = Double.valueOf(args[1]);
        int k = Integer.valueOf(args[2]);
	int r = Integer.valueOf(args[3]);
	//for (int i = 0; i<args.length;i++)
	//    print(args[i]);
        IM_flat imfl = new IM_flat(basename, beta, k, r);
	estimatedTime = System.currentTimeMillis() - startTime;
	print("Time elapsed = " + estimatedTime /(1000.0) + " sec");
    }
}

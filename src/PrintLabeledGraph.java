import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.Label;

public class PrintLabeledGraph {
    
    public static void print(String basename) throws Exception {
	System.out.println("Loading graph...");
	ArcLabelledImmutableGraph G = ArcLabelledImmutableGraph.load(basename+".w");
        System.out.println("Graph loaded");
        
        int n = G.numNodes();
        
        for(int v=0; v<n; v++) {
	    int[] v_neighbors = G.successorArray(v);
	    Label[] v_labels = G.labelArray(v);
            int v_deg = G.outdegree(v);
            
            for(int i=0; i<v_deg; i++) {
            	int u = v_neighbors[i];
            	Label label = v_labels[i];
            	System.out.println(v + "\t" + u + "\t" + label.getLong());
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
	if(args.length == 0) 
	    args = new String[]{"fang"};
	
	print(args[0]);
    }
}

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
public class Coarsen{
    String ext = "_temp";
    ArcLabelledImmutableGraph PG;
    int nodes;
    String basename;
    int r;
    ImmutableGraph FU;
    public Coarsen(String basename, int r) throws Exception{
	this.basename = basename;
	this.r = r;
	this.PG = ArcLabelledImmutableGraph.load("graphs/"+basename+".w");
	this.nodes = PG.numNodes();
	makeFinalUniverse();
    }
    public void makeFinalUniverse() throws Exception{
	Random rand = new Random();
	InstanceGraph Plast;
	InstanceGraph Pcurr;
	for (int q = 0; q<r; q++){
	    
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
		     if (rand.nextInt(1000) <= weight)
			 edges.add(u);
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
	    if (q == 0){
		Plast = new InstanceGraph();
	    }
	    else{
		Pcurr = new InstanceGraph();
	    }
	}
    }
    // inner class 1
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
    public class SCC{
	int [] scc;
	InstanceGraph graph;
	public SCC() throws Exception{
	    
	}
    }
}

//package grToEdge;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.lang.MutableString;
import java.util.*;
import com.google.common.collect.Sets;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.ImmutableSequentialGraph;
import java.io.*;


public class graphToEdgelist{
    public static void main(String[] args)throws Exception{
	long t1 = System.currentTimeMillis();
	String basename = args[0];
	/*
	String[] line = basename.split("/");
	int len = line.length;
	String output = line[len-1];
	*/
	System.out.println("Load Graph...");
	ImmutableGraph g = ImmutableGraph.loadMapped("webgraph/"+basename);
	int n = g.numNodes();
	PrintWriter writer = new PrintWriter("graphs/"+basename + ".edgelist");
	Random rnd = new Random();
	for (int i = 0 ; i < n ; i++){
	    //System.out.println("next i");
	    for (int neigh: g.successorArray(i)){
		// if (neigh > i){
		int x = rnd.nextInt(1000);
		writer.println(i + "\t" + neigh + "\t" + x );
	    }
	}
	writer.flush();
	System.out.println("Done! "+(System.currentTimeMillis()-t1)/1000.0+" seconds");
    }
}

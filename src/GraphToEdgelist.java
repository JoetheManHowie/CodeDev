/*
 * Joe Howie: April 10th 2021
 */
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


public class GraphToEdgelist{
    ImmutableGraph g;
    int n;
    String basename;
    public static <S> void print(S s){
	System.out.println(s);
    }
    public GraphToEdgelist(String basename)throws Exception{
	print("Load Graph...");
	g = ImmutableGraph.loadMapped("webgraph/"+basename);
	n = g.numNodes();
	this.basename = basename;
    }
    public void uniform()throws Exception{
	print("uniform chosen");
	PrintWriter uni_writer = new PrintWriter("graphs/"+basename + "_uniform.edgelist");;
	for (int i = 0 ; i < n ; i++){
	    for (int neigh: g.successorArray(i)){
		int x = 100; // 0.1
		uni_writer.println(i + "\t" + neigh + "\t" + x );
	    }
	}
	uni_writer.flush();
    }
    public void trivalency()throws Exception{ // unifom from set {0.1, 0.01, 0.001}
	print("trivalency chosen");
	Random tri_rnd = new Random();
	PrintWriter tri_writer = new PrintWriter("graphs/"+basename + "_trivalency.edgelist");;;
	for (int i = 0 ; i < n ; i++){
	    for (int neigh: g.successorArray(i)){
		int y = tri_rnd.nextInt(3);
		int x;
		if(y == 1) x = 100;
		else if(y == 2) x = 10;
		else x = 1;
		tri_writer.println(i + "\t" + neigh + "\t" + x );
	    }
	    tri_writer.flush();
	}
    }
    public void exponential()throws Exception{
	print("exponential chosen");
	Random exp_rnd = new Random();
	PrintWriter exp_writer = new PrintWriter("graphs/"+basename + "_exponential.edgelist");;;
	int lambda = 10; // mean =  1 / (lambda) = 0.1 in the paper
	int scaling = 100;
	for (int i = 0 ; i < n ; i++){
	    for (int neigh: g.successorArray(i)){ 
		double y = exp_rnd.nextInt(70)/100.0; // for D = [0, 0.69], R = [1, 1000]
		int x = (int)Math.round((scaling*lambda * Math.exp(-y * lambda)));
		exp_writer.println(i + "\t" + neigh + "\t" + x );
	    }
	}
	exp_writer.flush();
    }
    public void weighted()throws Exception{
	print("weighted chosen");
	PrintWriter wei_writer = new PrintWriter("graphs/"+basename + "_weighted.edgelist");;;
	for (int i = 0 ; i < n ; i++){
	    for (int neigh: g.successorArray(i)){
		int x = 10;
		wei_writer.println(i + "\t" + neigh + "\t" + x );
	    }
	}
	wei_writer.flush();
    }
    public static void main(String[] args)throws Exception{
	long t1 = System.currentTimeMillis();
	String basename = args[0];
	GraphToEdgelist edgelist = new GraphToEdgelist(basename);
	edgelist.trivalency();
	edgelist.exponential();
   	print("Done! "+(System.currentTimeMillis()-t1)/1000.0+" seconds");
    }
}

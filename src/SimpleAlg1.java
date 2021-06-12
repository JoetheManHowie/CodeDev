/* 
 * Joe Howie: June 3rd 2021
 * 
 * ex: 
 * 
 */
import java.lang.Math;
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
/**
 */
public class SimpleAlg1 {
    ArcLabelledImmutableGraph G;
    int n;
    long m;
    int [] deg_out;
    int [] deg_in;
    double [] deg_wout;
    double [] deg_win;
    double [] d_ratio; // in deg / out deg
    double ave_win, std_win;
    double ave_wout, std_wout;
    int [] hh; //  (high out, and in degree)
    int [] inf; // ratio < 1
    int [] sup; // ratio >= 1
    int [] speak_no_evil;
    int [] hear_no_evil;
    public static <S> void print(S s){
	System.out.println(s);
    }
    /**
     */
    public SimpleAlg1(String path, String basename) throws Exception{
	long time = System.currentTimeMillis();
	G = ArcLabelledImmutableGraph.load(path+"/"+basename+".w");
	n = G.numNodes();
	m = G.numArcs();
	print("n = "+n+", m = "+m);
	degree_calculation();
	//view();
	stat_calc();
	fill_arrays();
	print("Time Elapsed = "+(System.currentTimeMillis() - time)/1000.0);
    }
    /**
     */
    public void degree_calculation(){
	// run over
	deg_out = new int[n];
	deg_in  = new int[n];
	deg_wout = new double[n];
	deg_win  = new double[n];
	d_ratio = new double[n];
	for (int v = 0; v < n; v++){
	    int [] v_neighbours = G.successorArray(v);
	    Label [] v_labels = G.labelArray(v);
	    int v_deg = G.outdegree(v);
	    deg_out[v] = v_deg;
	    for (int i = 0; i < v_deg; i++){
		int u = v_neighbours[i];
		double weight = (double)v_labels[i].getLong()/1000.0;
		deg_in[u] += 1;
		deg_win[u] += weight;
		deg_wout[v] += weight;
		//print(v+"\t"+u+"\t"+weight);
	    }
	}
	int hne = 0, sne = 0;
	for (int v = 0; v < n; v++){
	    deg_win[v]  = deg_win[v]  * 1000;
	    deg_wout[v] = deg_wout[v] * 1000;
	    if (deg_in[v] == 0){
		d_ratio[v] = 0;
		hne++;
	    }
	    if(deg_out[v] == 0){
		d_ratio[v] = 0;
		sne++;
	    }else {
		d_ratio[v] = (deg_in[v] * deg_win[v]) / (deg_out[v] * deg_wout[v]);
	    }
	}
	hear_no_evil = new int [hne];
	speak_no_evil = new int[sne];
	print("No in deg = "+hne+" ratio = "+hne / (double)n * 100);
	print("No out degree = "+sne+" ratio = "+sne / (double)n * 100);
    }
    /**
     */
    public void stat_calc(){
	// get ave deg in and out by weight
	ave_win = 0;
	ave_wout = 0;
	for (int i = 0; i < n; i++){
	    ave_win  += deg_win[i];
	    ave_wout += deg_wout[i];
	}
	ave_win = ave_win/n;
	ave_wout = ave_wout/n;
	// get std by weight for in and out
	std_win = 0;
	std_wout = 0;
	for (int i = 0; i < n; i++){
	    std_win  += Math.pow(deg_win[i]  - ave_win , 2);
	    std_wout += Math.pow(deg_wout[i] - ave_wout , 2);
	}
	std_win  = Math.pow(std_win/(n-1), 0.5);
	std_wout = Math.pow(std_wout/(n-1), 0.5);
	print("ave in = "+ave_win +" std in = "+std_win);
	print("ave out = "+ave_wout+" std out = "+std_wout);
    }
    public void fill_arrays(){
	//set counters
	int count_hh  = 0;
	int count_inf = 0;
	int count_sup = 0;
	for (int i = 0; i < n; i++){
	    if (deg_win[i] > ave_win + std_win && deg_wout[i] > ave_wout + std_wout)
		count_hh++;
	    if (d_ratio[i] >= 1)
		count_sup++;
	    else
		count_inf++;
	}
	print("high in and out = "+count_hh+", ratio = "+count_hh / (double)n * 100);
	print("(r<1) = "+count_inf+", ratio = "+count_inf / (double)n * 100);
	print("(r>=1) = "+count_sup+", ratio = "+count_sup / (double)n * 100);
	
	hh  = new int[count_hh];
	inf = new int[count_inf];
	sup = new int[count_sup];
	// reset counters
	int count_hne = 0, count_sne = 0;
	count_hh = 0; count_inf = 0; count_sup = 0;
	for(int i = 0; i < n; i++){
	    if (deg_win[i] > ave_win + std_win && deg_wout[i] > ave_wout + std_wout){
		hh[count_hh] = i;
		count_hh++;
	    }
	    if (d_ratio[i] >= 1){
		sup[count_sup] = i;
		count_sup++;
	    }
	    else{
		inf[count_inf] = i;
		count_inf++;
	    }
	    if (deg_in[i] == 0){
		hear_no_evil[count_hne] = i;
		count_hne++;
	    }
	    if (deg_out[i] == 0){
		speak_no_evil[count_sne] = i;
		count_sne++;
	    }
	}
    }
    /**
     */
    public void view(){
	print("v\tin\tout");
	for (int v = 0; v < n; v++){
	    print(v+"\t"+deg_win[v]+"\t"+deg_wout[v]);
	}
    }
    /**
     * Main Method
     */
    public static void main(String []args) throws Exception{
	if(args.length < 2) {
            print("Specify: path, basename");
            System.exit(1);
        }
	String path = args[0];
	String basename = args[1];
	SimpleAlg1 test = new SimpleAlg1(path, basename);
    }
}

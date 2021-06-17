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
import java.text.DecimalFormat;
import com.google.common.collect.Sets;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.ImmutableSequentialGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import java.io.*;


public class getRatios{
    public static <S> void print(S s){
	System.out.println(s);
    }
    public static void main(String[] args)throws Exception{
	DecimalFormat dec = new DecimalFormat("#.###");
	String basename0 = args[0];
	String basename1 = args[1];
		
	ArcLabelledImmutableGraph og = ArcLabelledImmutableGraph.load(basename0+".w");
	ArcLabelledImmutableGraph su = ArcLabelledImmutableGraph.load(basename1+".w");
	
	double V = (double)og.numNodes();
	int W = su.numNodes();
	double E = (double)og.numArcs();
	long F = su.numArcs();

	print("|W| = "+W+" |F| = "+F+" |W|/|V| = "+dec.format(W/V*100)+" |F|/|E| = "+dec.format(F/E*100));
    }
}

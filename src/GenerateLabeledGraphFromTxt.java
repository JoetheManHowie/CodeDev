import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;

import it.unimi.dsi.io.OutputBitStream;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.BitStreamArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.GammaCodedIntLabel;

public class GenerateLabeledGraphFromTxt {

	ImmutableGraph G;
    int n;
	long m;
	String basename;
	String weightfilename;
	
	public GenerateLabeledGraphFromTxt(String basename, String weightfilename) throws Exception {
		
		this.basename = basename;
		this.weightfilename = weightfilename;
		
		this.G = ImmutableGraph.loadMapped(basename);
	}
	
	public void store() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File(weightfilename)));
		
		int STD_BUFFER_SIZE = 1024 * 1024;
		ProgressLogger pl = new ProgressLogger();
		GammaCodedIntLabel prototype = new GammaCodedIntLabel( "FOO" );
		GammaCodedIntLabel label = prototype.copy();
		
		final OutputBitStream labels = new OutputBitStream( basename + ".w" +
				BitStreamArcLabelledImmutableGraph.LABELS_EXTENSION, STD_BUFFER_SIZE ); 
		final OutputBitStream offsets = new OutputBitStream( basename + ".w" + 
				BitStreamArcLabelledImmutableGraph.LABEL_OFFSETS_EXTENSION, STD_BUFFER_SIZE );

		if ( pl != null ) {
			pl.itemsName = "nodes";
			pl.expectedUpdates = G.numNodes();
			pl.start( "Saving labels..." );
		}

		final NodeIterator nodeIterator = G.nodeIterator();
		offsets.writeGamma( 0 );
		int curr;
		long count;
		LazyIntIterator successors;

		while( nodeIterator.hasNext() ) {
			curr = nodeIterator.nextInt();
			successors = nodeIterator.successors();
			count = 0;
			while( successors.nextInt() != -1 ) {
				label.value = Integer.parseInt(br.readLine().split("\\s+")[2]); 
				count += label.toBitStream( labels, curr );
			}
			offsets.writeLongGamma( count );
			if ( pl != null ) pl.lightUpdate();
		}
		
		if ( pl != null ) pl.done();
		labels.close();
		offsets.close();
		
		final PrintWriter properties = new PrintWriter( new FileOutputStream( basename + ".w" + ImmutableGraph.PROPERTIES_EXTENSION ) );
		properties.println( ImmutableGraph.GRAPHCLASS_PROPERTY_KEY + " = " + BitStreamArcLabelledImmutableGraph.class.getName() );
		properties.println( ArcLabelledImmutableGraph.UNDERLYINGGRAPH_PROPERTY_KEY + " = " + basename );
		properties.println( BitStreamArcLabelledImmutableGraph.LABELSPEC_PROPERTY_KEY + " = " + prototype.toSpec() );
		properties.close();
		
		br.close();
	}
	
	public static void main(String[] args) throws Exception {	
		if(args.length == 0)
			args = new String[] {"smallexample", "smallexample.txt"};
		
		GenerateLabeledGraphFromTxt gen = new GenerateLabeledGraphFromTxt(args[0], args[1]);
		gen.store();
	}
}

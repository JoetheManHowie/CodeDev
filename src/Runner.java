/*
 * Joe Howie March 10th
 *
 * java -Xss2g -Xmx512m -cp "bin":"lib/*" Runner basename integer
 *
 * OPTION: nohup java -Xss4g -Xmx64g -cp "bin":"lib/*" Runner basename integer > basename.out 2>&1&
 */
import java.text.DecimalFormat;
public class Runner{

    public static <S> void print(S s){
	System.out.println(s);
    }
    
    public static void main(String [] args) throws Exception{
	long startTime = System.currentTimeMillis();
	if(args.length == 0){
	    print("the if happened");
	    args = new String[]{"fang"};
	}
	String basename = args[0];
	int number_of_world = Integer.parseInt(args[1]);
	print("Running : " +basename);
	Coarsen C = new Coarsen(basename, number_of_world);
	DecimalFormat dec = new DecimalFormat("#.###");
	print("Results for "+basename);
	print("|V| = "+ dec.format(C.nodes)+", |E| = "+ dec.format(C.edges));
	print("|F| = " + dec.format(C.H.F_size()) + ", |F|/|E| = " + dec.format(C.getEdgeRatio()) + ", |W| = "+ dec.format(C.H.W_size()) + ", |W|/|V| = " + dec.format(C.getVertexRatio()));
	print("Total time elapsed = "+(System.currentTimeMillis()-startTime)/1000.0 +" seconds");
    }

}

/*
 * Joe Howie March 10th
 */

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
	print("Total time elapsed = "+(System.currentTimeMillis()-startTime)/1000.0 +" seconds");
    }

}

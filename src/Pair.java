
import java.util.*;
public class Pair{
    public final Integer i;
    public final Integer j;
    public Pair(int i, int j){
	this.i = new Integer(Integer.valueOf(i));
	this.j = new Integer(Integer.valueOf(j));
    }
    // src: https://bit.ly/3eK1P1B
    @Override
    public int hashCode(){
	return i.hashCode() ^ j.hashCode();
    }
    @Override
    public boolean equals(Object obj){
	return (obj instanceof Pair ) && ((Pair) obj).i.equals(i) && ((Pair) obj).j.equals(j);
    }
}

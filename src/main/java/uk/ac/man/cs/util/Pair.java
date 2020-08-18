package uk.ac.man.cs.util;

/**
 * Created by chris on 06/04/18.
 */
public class Pair<A,B> {

    //public final A first;
    //public final B second;
    private A first;
    private B second;

    public Pair(A a, B b){
        this.first = a;
        this.second = b;
    }

    public A getFirst(){
        return this.first; 
    }
    public B getSecond(){
        return this.second; 
    }

    public String toString(){
        return "(" + this.first.toString() + "," + this.second.toString() + ")"; 
    }
}

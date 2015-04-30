package uk.ac.shef.dcs.oak.util;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 29/04/13
 * Time: 10:43
 */
public class PairOfObjects<E> {

    private E first;
    private E second;

    public PairOfObjects(E first, E second){
        this.first=first;
        this.second=second;
    }

    public E getFirst() {
        return first;
    }

    public void setFirst(E first) {
        this.first = first;
    }

    public E getSecond() {
        return second;
    }

    public void setSecond(E second) {
        this.second = second;
    }

    public boolean equals(Object o){
        if(o instanceof PairOfObjects){
            PairOfObjects that = (PairOfObjects)o;
            return this.first.equals(that.getFirst()) &&this.second.equals(that.getSecond());
        }
        return false;
    }

    public int hashCode(){
        return this.first.hashCode()+19*this.second.hashCode();
    }
}

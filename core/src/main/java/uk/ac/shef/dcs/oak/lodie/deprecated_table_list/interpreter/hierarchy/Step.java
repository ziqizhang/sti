package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.hierarchy;

import java.util.ArrayList;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 26/04/13
 * Time: 17:25
 */
public class Step extends ArrayList<String> implements Comparable<Step>{
    private int level;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int compareTo(Step o) {
        return Integer.valueOf(getLevel()).compareTo(o.getLevel());
    }

    public boolean equals(Object o){
        if(o instanceof Step){
            Step that = (Step) o;
            if(this.size()==that.size()){
                int count=0;
                for(String a: this){
                    for(String b:that){
                        if(a.equals(b)){
                            count++;
                            break;
                        }
                    }
                }
                return count==this.size();
            }
        }
        return false;
    }

    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append("(").append(getLevel()).append(")");
        for(String l : this){
            s.append(l).append("|");
        }
        return s.toString();
    }
}

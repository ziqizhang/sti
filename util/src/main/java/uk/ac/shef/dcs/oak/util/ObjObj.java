package uk.ac.shef.dcs.oak.util;

import java.io.Serializable;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 07/05/13
 * Time: 17:14
 */
public class ObjObj<K, V> implements Serializable {
    private K mainObject;
    private V otherObject;

    public ObjObj(){}

    public ObjObj(K k, V v){
        mainObject=k;
        otherObject=v;
    }

    public K getMainObject() {
        return mainObject;
    }

    public void setMainObject(K mainObject) {
        this.mainObject = mainObject;
    }

    public V getOtherObject() {
        return otherObject;
    }

    public void setOtherObject(V otherObject) {
        this.otherObject = otherObject;
    }

    public String toString(){
        return getMainObject()+","+getOtherObject();
    }

}

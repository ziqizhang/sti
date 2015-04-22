package uk.ac.shef.dcs.oak.triplesearch.util;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 24/04/13
 * Time: 21:01
 */
public class StopLists {

    //todo: this class strictly speaking should query sparql, to see if its superclass is "thing". if so, discard
        public static boolean isMeaninglessClass(String classURI){
            if(classURI.endsWith("core#Concept"))
                return true;
            if(classURI.endsWith("owl#Thing"))
                return true;
            if(classURI.endsWith("owl#Class"))
                return true;
            return false;
        }
    }

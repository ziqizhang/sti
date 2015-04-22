package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.content.KBSearcher;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.KB_InstanceFilter;
import uk.ac.shef.dcs.oak.lodie.table.rep.HeaderAnnotation;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.io.IOException;
import java.util.*;

/**
 * Created by zqz on 20/04/2015.
 */
public class ColumnClassifier {

    private KBSearcher kbSearcher;

    public ColumnClassifier(KBSearcher kbSearcher){
        this.kbSearcher = kbSearcher;
    }

    public List<ObjObj<String, Double>> rankColumnConcepts(int col, ObjectMatrix2D entityCandidates) throws IOException {
        ObjectMatrix1D column = entityCandidates.viewColumn(col);

        Map<String, Double> votes = new HashMap<String, Double>();
        for(int r=0; r<column.size(); r++){
            List<ObjObj<EntityCandidate, Double>> neCandidates= (List<ObjObj<EntityCandidate, Double>>)column.get(r);
            if(neCandidates!=null&&neCandidates.size()>0){
                EntityCandidate e = neCandidates.get(0).getMainObject();
                List<String> types = e.getTypeIds();
                for(String t: types){
                    if(KB_InstanceFilter.ignoreType(t, t))
                        continue;
                    Double v = votes.get(t);
                    v= v==null?1.0:v;
                    v+=1.0;
                    votes.put(t, v);
                }
            }
        }

        List<ObjObj<String, Double>> result = new ArrayList<ObjObj<String, Double>>();
        for(Map.Entry<String, Double> e: votes.entrySet()){
            result.add(new ObjObj<String, Double>(e.getKey(), e.getValue()/column.size()));
        }
        Collections.sort(result, new Comparator<ObjObj<String, Double>>() {
            @Override
            public int compare(ObjObj<String, Double> o1, ObjObj<String, Double> o2) {
                return o2.getOtherObject().compareTo(o1.getOtherObject());
            }
        });

        //tie breaker
        double maxScore = result.get(0).getOtherObject();
        final Map<ObjObj<String, Double>, Double> granularityScore = new HashMap<ObjObj<String, Double>, Double>();
        for(ObjObj<String, Double> e: result){
            if(e.getOtherObject()==maxScore){
                granularityScore.put(e, kbSearcher.find_granularityForType(e.getMainObject()));
            }
        }
        if(granularityScore.size()<2)
            return result;

        Collections.sort(result, new Comparator<ObjObj<String, Double>>() {
            @Override
            public int compare(ObjObj<String, Double> o1, ObjObj<String, Double> o2) {
                if(o2.getOtherObject()==o1.getOtherObject()) {
                    Double o2Granularity = granularityScore.get(o2);
                    Double o1Granularity=granularityScore.get(o1);
                    if(o1Granularity!=-1&&o2Granularity!=-1)
                        return o2Granularity.compareTo(o1Granularity);
                    else return 0;
                }
                return o2.getOtherObject().compareTo(o1.getOtherObject());
            }
        });
        return result;
    }
}

package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.content.KBSearcher;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.KB_InstanceFilter;
import uk.ac.shef.dcs.oak.lodie.table.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.HeaderAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableAnnotation;
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

    public List<ObjObj<String, Double>> rankColumnConcepts(LTableAnnotation tableAnnotation, LTable table, int col) throws IOException {
        Map<String, Double> votes = new HashMap<String, Double>();
        for(int r=0; r<table.getNumRows(); r++){
            CellAnnotation[] cellAnnotations = tableAnnotation.getContentCellAnnotations(r, col);
            if(cellAnnotations!=null&&cellAnnotations.length>0){
                EntityCandidate e = cellAnnotations[0].getAnnotation();
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
            result.add(new ObjObj<String, Double>(e.getKey(), e.getValue()/table.getNumRows()));
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

        HeaderAnnotation[] headerAnnotations = new HeaderAnnotation[result.size()];
        int i=0;
        for(ObjObj<String, Double> oo: result){
            HeaderAnnotation ha = new HeaderAnnotation(table.getColumnHeader(col).getHeaderText(),
                    oo.getMainObject(),oo.getMainObject(), oo.getOtherObject());
            headerAnnotations[i]=ha;
            i++;
        }
        tableAnnotation.setHeaderAnnotation(col, headerAnnotations);
        return result;
    }
}

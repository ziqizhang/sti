package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import cern.colt.matrix.ObjectMatrix2D;
import org.openjena.atlas.iterator.Iter;
import uk.ac.shef.dcs.oak.lodie.table.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableAnnotation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zqz on 23/04/2015.
 */
public class CellAnnotationUpdater {

    private double minConfidence = 0.0;

    public CellAnnotationUpdater() {

    }

    public CellAnnotationUpdater(double minConfidence) {
        this.minConfidence = minConfidence;
    }

    public void update(ObjectMatrix2D messages, LTableAnnotation tableAnnotation) {
        for (int r = 0; r < messages.rows(); r++) {
            for (int c = 0; c < messages.columns(); c++) {
                Object container = messages.get(r, c);
                if (container == null)
                    continue;

                List<ChangeMessage> messages_for_cell = (List<ChangeMessage>) container;
                Collections.sort(messages_for_cell);

                ChangeMessage m = messages_for_cell.get(0);
                updateCellAnnotation(r, c, m, tableAnnotation);

            }
        }
    }

    private void updateCellAnnotation(int r, int c, ChangeMessage m, LTableAnnotation annotation) {
        CellAnnotation[] cellAnnotations = annotation.getContentCellAnnotations(r, c);
        double maxScore = cellAnnotations[0].getFinalScore();
        double artificialMaxScore = maxScore+0.1;

        if(m instanceof ChangeMessageFromColumnsRelation){
            ChangeMessageFromColumnsRelation message = (ChangeMessageFromColumnsRelation) m;
            if(message.getFlag_subOrObj()==0){ //the current cell's NE is the subject in the relation that sends the "change" message
                List<CellAnnotation> list = Arrays.asList(cellAnnotations);
                Iterator<CellAnnotation> it = list.iterator();
                double maxScore_of_matched = 0.0;
                while(it.hasNext()){
                    CellAnnotation next = it.next();
                    List<String[]> facts = next.getAnnotation().getFacts();
                    if(!containsRelation(facts, message.getLabel()))
                        continue;
                    if(next.getAnnotation().getTypes().contains(m.getLabel())){
                        if(maxScore_of_matched==0.0)
                            maxScore_of_matched=next.getFinalScore();

                        if(next.getFinalScore()==maxScore_of_matched){
                            next.setFinalScore(artificialMaxScore);
                        }
                    }
                }
                Collections.sort(list);
                annotation.setContentCellAnnotations(r, c, list.toArray(new CellAnnotation[0]));
            }else{

            }
        }
        else{
            List<CellAnnotation> list = Arrays.asList(cellAnnotations);
            Iterator<CellAnnotation> it = list.iterator();
            double maxScore_of_matched = 0.0;
            while(it.hasNext()){
                CellAnnotation next = it.next();
                if(next.getAnnotation().getTypes().contains(m.getLabel())){
                    if(maxScore_of_matched==0.0)
                        maxScore_of_matched=next.getFinalScore();

                    if(next.getFinalScore()==maxScore_of_matched){
                        next.setFinalScore(artificialMaxScore);
                    }
                }
            }
            Collections.sort(list);
            annotation.setContentCellAnnotations(r, c, list.toArray(new CellAnnotation[0]));
        }
    }

    private boolean containsRelation(List<String[]> facts, String label) {
        for(String[] fact: facts){
            if(fact[0].equals(label))
                return true;
        }
        return false;
    }
}

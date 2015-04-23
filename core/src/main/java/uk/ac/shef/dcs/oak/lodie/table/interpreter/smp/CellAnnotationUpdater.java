package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import cern.colt.matrix.ObjectMatrix2D;
import org.openjena.atlas.iterator.Iter;
import uk.ac.shef.dcs.oak.lodie.table.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.Key_SubjectCol_ObjectCol;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableAnnotation;

import java.util.*;

/**
 * Created by zqz on 23/04/2015.
 */
public class CellAnnotationUpdater {

    protected static double minConfidence = 0.0;

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
                checkMinConfidence(messages_for_cell);
                Collections.sort(messages_for_cell);
                List<String> messagesPreferenceSorted = createSortedPreferenceKeys(messages_for_cell.size());

                Map<Integer, List<Integer>> annotation_satisfies_messages =
                        computeSatisfiedMessages(r, c, messages_for_cell, tableAnnotation);
                List<Integer> bestAnnotations = select(annotation_satisfies_messages, messagesPreferenceSorted);

                //todo: if no annotation can satisfy any prefrence, the cell is updated to "no annotation"

                //retrieve actual annotation based on their indexes...
                CellAnnotation[] currentAnnotations = tableAnnotation.getContentCellAnnotations(r,c);
                double currentMaxScore = currentAnnotations[0].getFinalScore();
                double arbitraryNewScore = currentMaxScore+0.000001;
                for(int i=0; i<currentAnnotations.length; i++){
                    if(bestAnnotations.contains(i))
                        currentAnnotations[i].setFinalScore(arbitraryNewScore);
                }
                List<CellAnnotation> list = Arrays.asList(currentAnnotations);
                Collections.sort(list);
                tableAnnotation.setContentCellAnnotations(r,c, list.toArray(new CellAnnotation[0]));
            }
        }
    }

    protected static void checkMinConfidence(List<ChangeMessage> messages_for_cell) {
        Iterator<ChangeMessage> it = messages_for_cell.iterator();
        while(it.hasNext()){
            ChangeMessage m = it.next();
            if (m.getConfidence()<minConfidence)
                it.remove();
        }
    }

    private Map<Integer, List<Integer>> computeSatisfiedMessages(int r, int c,
                                                                 List<ChangeMessage> messages_for_cell,
                                                                 LTableAnnotation tableAnnotation) {
        CellAnnotation[] cellAnnotations = tableAnnotation.getContentCellAnnotations(r, c);
        Map<Integer, List<Integer>> out = new HashMap<Integer, List<Integer>>();
        if(cellAnnotations==null||cellAnnotations.length==0) //possible if message is sent by relation and request a change to its object which is not an NE
            return out;

        for (int i = 0; i < cellAnnotations.length; i++) {
            CellAnnotation ca = cellAnnotations[i];
            List<Integer> satisfied_messages = new ArrayList<Integer>();

            for (int j = 0; j < messages_for_cell.size(); j++) {
                ChangeMessage m = messages_for_cell.get(j);
                boolean satisfied = checkEntityAgainstMessage(ca,r,c, m,tableAnnotation);
                if (satisfied)
                    satisfied_messages.add(j);
            }

            out.put(i, satisfied_messages);
        }
        return out;
    }

    private boolean checkEntityAgainstMessage(CellAnnotation ca, int row, int col, ChangeMessage m, LTableAnnotation tableAnnotation) {
        if (m instanceof ChangeMessageFromColumnsRelation) {
            ChangeMessageFromColumnsRelation message = (ChangeMessageFromColumnsRelation) m;
            if (message.getFlag_subOrObj() == 0) { //the current cell's NE is the subject in the relation that sends the "change" message
                List<String[]> facts = ca.getAnnotation().getFacts();
                if (containsRelation(facts, message.getLabel()))
                    return true;
                else return false;
            }else{
                //find the subject CellAnnotation
                for(Key_SubjectCol_ObjectCol relation_subobjKey: tableAnnotation.getRelationAnnotations_across_columns().keySet()){
                    if(relation_subobjKey.getObjectCol()==col) {
                        CellAnnotation[] subjectCellAnnotations = tableAnnotation.getContentCellAnnotations(row, relation_subobjKey.getSubjectCol());
                        //check if any fact of the subject CellAnnotation mentions ca
                        for(CellAnnotation subjectCellAnnotation: subjectCellAnnotations){
                            for(String[] fact : subjectCellAnnotation.getAnnotation().getFacts()){
                                if(fact[1].equals(ca.getAnnotation().getId())){
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (ca.getAnnotation().getTypes().contains(m.getLabel()))
                return true;
        }
        return false;
    }

    //key-index of the CellAnnotation in the list of candidate CellAnnotations for the cell ranked by their score
    //value-list of indexes of satisfied messages by this CellAnnotation.
    private List<Integer> select(Map<Integer, List<Integer>> annotation_satisfies_messages,
                                 List<String> messagePreferencesSorted) {
        int bestPreferenceIndex = Integer.MAX_VALUE;
        List<Integer> bestAnnotations = new ArrayList<Integer>();
        if(annotation_satisfies_messages.size()==0)
            return bestAnnotations;
        for (Map.Entry<Integer, List<Integer>> e : annotation_satisfies_messages.entrySet()) {
            List<Integer> messages_satisfied = e.getValue();
            Collections.sort(messages_satisfied);

            String key = "";
            for (Integer m : messages_satisfied)
                key += m + ",";
            key = key.substring(0, key.length() - 1).trim();

            int index = messagePreferencesSorted.indexOf(key);
            if (index < bestPreferenceIndex) {
                bestPreferenceIndex = index;
            }
        }

        String bestPreference = messagePreferencesSorted.get(bestPreferenceIndex);
        for (Map.Entry<Integer, List<Integer>> e : annotation_satisfies_messages.entrySet()) {
            List<Integer> messages_satisfied = e.getValue();

            String key = "";
            for (Integer m : messages_satisfied)
                key += m + ",";
            key = key.substring(0, key.length() - 1).trim();

            if (key.equals(bestPreference))
                bestAnnotations.add(e.getKey());
        }
        return bestAnnotations;
    }

    //to update cell's annotation, we need to select the one that satisfies most "change" messages sent from different
    //factors. Here we compute different combinations of satisfied "change" messages ranked by preference (see Mulwad's papge
    // on page 12
    private List<String> createSortedPreferenceKeys(int totalMessages) {
        List<String> rs = new ArrayList<String>();
        for (int i = 0; i < totalMessages; i++) {
            String s = String.valueOf(i) + ",";
            for (int j = 1; j < totalMessages; j++) {
                s += String.valueOf(j) + ",";
            }
            if (s.endsWith(","))
                s = s.substring(0, s.length() - 1).trim();
            rs.add(s);
        }
        Collections.sort(rs, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Integer length1 = o1.length();
                Integer length2 = o2.length();
                int compare = length2.compareTo(length1);
                if (compare == 0) {
                    return o1.compareTo(o2);
                } else
                    return compare;
            }
        });
        return rs;
    }

    private boolean containsRelation(List<String[]> facts, String label) {
        for (String[] fact : facts) {
            if (fact[0].equals(label))
                return true;
        }
        return false;
    }
}

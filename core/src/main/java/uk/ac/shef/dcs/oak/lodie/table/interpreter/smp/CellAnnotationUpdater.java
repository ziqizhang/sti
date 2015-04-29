package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import cern.colt.matrix.ObjectMatrix2D;
import org.openjena.atlas.iterator.Iter;
import uk.ac.shef.dcs.oak.lodie.table.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.Key_SubjectCol_ObjectCol;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.util.CombinationGenerator;

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

    public int[] update(ObjectMatrix2D messages, LTableAnnotation tableAnnotation) {
        int countUpdateNeeded = 0, countUpdated = 0;// an update is invalid if the message requires the cell to change to
        //the same NE that it was already assigned to
        //this is possible due to relation messages
        //go thru each cell
        for (int r = 0; r < messages.rows(); r++) {
            for (int c = 0; c < messages.columns(); c++) {
                Object container = messages.get(r, c);
                if (container == null)
                    continue;
                countUpdateNeeded++;
                List<ChangeMessage> messages_for_cell = (List<ChangeMessage>) container;
                if(messages_for_cell.size()==0)
                    continue;
                checkMinConfidence(messages_for_cell);
                Collections.sort(messages_for_cell);
                //not all messages can be satisfied. compute the preferences of the combinations of messages satisfied
                List<String> messagesPreferenceSorted = createSortedPreferenceKeys(messages_for_cell.size());
                //key- the index of CellAnnotation in the CellAnnotation[] object returned by #tableAnnotation.get...(r, c)
                //value- list of indexes (as appearing in messages_for_cell of messages satisfied by the CellAnnotation identified by 'key'
                Map<Integer, List<Integer>> annotation_satisfies_messages =
                        computeSatisfiedMessages(r, c, messages_for_cell, tableAnnotation);
                //indexes of candidate entity annotation for the cell (r, c)
                List<Integer> bestAnnotations = select(annotation_satisfies_messages, messagesPreferenceSorted);

                //retrieve actual annotation based on their indexes...
                if (bestAnnotations.size() > 0) {
                    boolean isValidUpdate = false;
                    CellAnnotation[] candidateAnnotations = tableAnnotation.getContentCellAnnotations(r, c);
                    List<CellAnnotation> currentBestAnnotations = tableAnnotation.getBestContentCellAnnotations(r, c);
                    double currentMaxScore = currentBestAnnotations.get(0).getFinalScore();
                    double arbitraryNewScore = currentMaxScore + 0.000001;
                    for (int i = 0; i < candidateAnnotations.length; i++) {
                        if (bestAnnotations.contains(i)) {
                            if (candidateAnnotations[i].getFinalScore() != currentMaxScore) //the chosen best annotation tobe updated
                                //is not already the highest scoring candidate
                                isValidUpdate = true;
                            candidateAnnotations[i].setFinalScore(arbitraryNewScore);
                        }

                    }
                    List<CellAnnotation> list = Arrays.asList(candidateAnnotations);
                    Collections.sort(list);
                    tableAnnotation.setContentCellAnnotations(r, c, list.toArray(new CellAnnotation[0]));
                    if(isValidUpdate)
                        countUpdated++;
                    else
                        countUpdateNeeded--;
                }
            }
        }
        return new int[]{countUpdated, countUpdateNeeded};
    }

    protected static void checkMinConfidence(List<ChangeMessage> messages_for_cell) {
        Iterator<ChangeMessage> it = messages_for_cell.iterator();
        while (it.hasNext()) {
            ChangeMessage m = it.next();
            if (m.getConfidence() < minConfidence)
                it.remove();
        }
    }

    /**
     * go thru every candidate cell annotation in cell (r, c), count for each candidate, the ChangeMessage objects that
     * can be satisfied and record their index appearing "messages_for_cell"
     *
     * @param r                 row id
     * @param c                 column id
     * @param messages_for_cell change messages for the cell (r, c)
     * @param tableAnnotation
     * @return key- the index of CellAnnotation in the CellAnnotation[] object returned by #tableAnnotation.get...(r, c)
     * value- list of indexes (as appearing in messages_for_cell of messages satisfied by the CellAnnotation identified by 'key'
     */
    private Map<Integer, List<Integer>> computeSatisfiedMessages(int r, int c,
                                                                 List<ChangeMessage> messages_for_cell,
                                                                 LTableAnnotation tableAnnotation) {
        CellAnnotation[] cellAnnotations = tableAnnotation.getContentCellAnnotations(r, c);
        Map<Integer, List<Integer>> out = new HashMap<Integer, List<Integer>>();
        if (cellAnnotations == null || cellAnnotations.length == 0) //possible if message is sent by relation and request a change to its object which is not an NE
            return out;

        for (int i = 0; i < cellAnnotations.length; i++) {    //go through every candidate annotation for the cell, count # of messages that each candidate satisfies
            CellAnnotation ca = cellAnnotations[i];
            List<Integer> satisfied_messages = new ArrayList<Integer>();

            for (int j = 0; j < messages_for_cell.size(); j++) { //go through each message to check against that candidate
                ChangeMessage m = messages_for_cell.get(j);
                boolean satisfied = checkEntityAgainstMessage(ca, r, c, m, tableAnnotation);
                if (satisfied)
                    satisfied_messages.add(j);
            }
            if (satisfied_messages.size() > 0)
                out.put(i, satisfied_messages);
        }
        return out;
    }

    private boolean checkEntityAgainstMessage(CellAnnotation ca, int row, int col, ChangeMessage m, LTableAnnotation tableAnnotation) {
        //if the change message is due to relation
        if (m instanceof ChangeMessageFromColumnsRelation) {
            ChangeMessageFromColumnsRelation message = (ChangeMessageFromColumnsRelation) m;
            if (message.getFlag_subOrObj() == 0) { //the current cell's NE is the subject in the relation that sends the "change" message
                //we need to fetch all facts of a candidate entity annotation, check if any fact uses a relation same as identified
                //in the message
                List<String[]> facts = ca.getAnnotation().getFacts();
                if (containsRelation(facts, message.getLabels()))
                    return true;
                else return false;
            } else { //if the current cell's NE is the object in the relation, first we need to
                //find the subject CellAnnotation
                for (Key_SubjectCol_ObjectCol relation_subobjKey : tableAnnotation.getRelationAnnotations_across_columns().keySet()) {
                    if (relation_subobjKey.getObjectCol() == col) {
                        CellAnnotation[] subjectCellAnnotations = tableAnnotation.getContentCellAnnotations(row, relation_subobjKey.getSubjectCol());
                        //check if any fact of any candidate subject CellAnnotation mentions ca
                        //note that we do not need to fix the candidate subject CellAnnotation at this stage.
                        //because another message should have been sent to the cell that corresponds to the subject cell
                        //and that ensures the subject's cell's annotation will be dealt with separately
                        for (CellAnnotation subjectCellAnnotation : subjectCellAnnotations) {
                            for (String[] fact : subjectCellAnnotation.getAnnotation().getFacts()) {
                                if (fact[1].equals(ca.getAnnotation().getId())) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else {  //change message sent by header
            List<String> legitHeaderLabels = new ArrayList<String>(m.getLabels());
            legitHeaderLabels.retainAll(ca.getAnnotation().getTypeIds());
            if (legitHeaderLabels.size() > 0)
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
        if (annotation_satisfies_messages.size() == 0)
            return bestAnnotations;

        //debugging//
        /*if(annotation_satisfies_messages.size()>1)
            System.out.println();*/
        //

        for (Map.Entry<Integer, List<Integer>> e : annotation_satisfies_messages.entrySet()) {
            List<Integer> messages_satisfied = e.getValue();
            Collections.sort(messages_satisfied);

            String key = "";
            for (Integer m : messages_satisfied)
                key += m + " ";
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
                key += m + " ";
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
        String[] in = new String[totalMessages];
        for (int i = 0; i < totalMessages; i++) {
            in[i] = String.valueOf(i);
        }
        List<String> rs = CombinationGenerator.generateCombinations(in);
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

    private boolean containsRelation(List<String[]> facts, List<String> labels) {
        for (String[] fact : facts) {
            if (labels.contains(fact[0]))
                return true;
        }
        return false;
    }
}

package uk.ac.shef.dcs.sti.core.algorithm.smp;

import cern.colt.matrix.ObjectMatrix2D;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.util.SubsetGenerator;

import java.util.*;

/**
 * Created by zqz on 23/04/2015.
 */
class CellAnnotationUpdater {

    public int[] update(ObjectMatrix2D messages, TAnnotation tableAnnotation) {
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
                @SuppressWarnings("unchecked")
                List<ChangeMessage> messages_for_cell = (List<ChangeMessage>) container;
                if(messages_for_cell.size()==0)
                    continue;
                Collections.sort(messages_for_cell);
                //not all messages can be satisfied. compute the preferences of the combinations of messages satisfied
                List<String> messagesPreferenceSorted = createSortedPreferenceKeys(messages_for_cell.size());

                TCellAnnotation[] candidateAnnotations = tableAnnotation.getContentCellAnnotations(r, c);
                //key- the index of TCellAnnotation in the TCellAnnotation[] object returned by #tableAnnotation.get...(r, c)
                //value- list of indexes (as appearing in messages_for_cell of messages satisfied by the TCellAnnotation identified by 'key'
                Map<Integer, List<Integer>> annotation_satisfies_messages =
                        computeSatisfiedMessages(r, c, messages_for_cell, tableAnnotation);
                //indexes of candidate entity annotation for the cell (r, c)
                List<Integer> bestAnnotations = select(annotation_satisfies_messages, messagesPreferenceSorted, candidateAnnotations);

                //retrieve actual annotation based on their indexes...
                if (bestAnnotations.size() > 0) {
                    boolean isValidUpdate = false;
                    List<TCellAnnotation> currentWinningAnnotations = tableAnnotation.getWinningContentCellAnnotation(r, c);
                    double currentMaxScore = currentWinningAnnotations.get(0).getFinalScore();
                    double arbitraryNewScore = currentMaxScore + 0.000001;

                    for (int i = 0; i < candidateAnnotations.length; i++) {
                        if (bestAnnotations.contains(i)) {
                            if (candidateAnnotations[i].getFinalScore() != currentMaxScore) //the chosen best annotation tobe updated
                                //is not already the highest scoring candidate
                                isValidUpdate = true;
                            candidateAnnotations[i].setFinalScore(arbitraryNewScore);
                        }

                    }
                    Arrays.sort(candidateAnnotations);
                    tableAnnotation.setContentCellAnnotations(r, c, candidateAnnotations);
                    if(isValidUpdate)
                        countUpdated++;
                    else
                        countUpdateNeeded--;
                }
            }
        }
        return new int[]{countUpdated, countUpdateNeeded};
    }



    /**
     * go thru every candidate cell annotation in cell (r, c), count for each candidate, the ChangeMessage objects that
     * can be satisfied and record their index appearing "messages_for_cell"
     *
     * @param r                 row id
     * @param c                 column id
     * @param messages_for_cell change messages for the cell (r, c)
     * @param tableAnnotation
     * @return key- the index of TCellAnnotation in the TCellAnnotation[] object returned by #tableAnnotation.get...(r, c)
     * value- list of indexes (as appearing in messages_for_cell of messages satisfied by the TCellAnnotation identified by 'key'
     */
    private Map<Integer, List<Integer>> computeSatisfiedMessages(int r, int c,
                                                                 List<ChangeMessage> messages_for_cell,
                                                                 TAnnotation tableAnnotation) {
        TCellAnnotation[] cellAnnotations = tableAnnotation.getContentCellAnnotations(r, c);
        Map<Integer, List<Integer>> out = new HashMap<>();
        if (cellAnnotations == null || cellAnnotations.length == 0) //possible if message is sent by relation and request a change to its object which is not an NE
            return out;

        for (int i = 0; i < cellAnnotations.length; i++) {    //go through every candidate annotation for the cell, count # of messages that each candidate satisfies
            TCellAnnotation ca = cellAnnotations[i];
            List<Integer> satisfied_messages = new ArrayList<>();

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

    private boolean checkEntityAgainstMessage(TCellAnnotation ca, int row, int col, ChangeMessage m, TAnnotation tableAnnotation) {
        //if the change message is due to relation
        if (m instanceof ChangeMessageFromRelation) {
            ChangeMessageFromRelation message = (ChangeMessageFromRelation) m;
            if (message.getSubobjIndicator() == 0) { //the current cell's NE is the subject in the relation that sends the "change" message
                //we need to fetch all facts of a candidate entity annotation, check if any fact uses a relation same as identified
                //in the message
                List<Attribute> facts = ca.getAnnotation().getAttributes();
                if (containsRelation(facts, message.getLabels()))
                    return true;
                else return false;
            } else { //if the current cell's NE is the object in the relation, first we need to
                //find the subject TCellAnnotation
                for (RelationColumns relation_subobjKey : tableAnnotation.getColumncolumnRelations().keySet()) {
                    if (relation_subobjKey.getObjectCol() == col) {
                        TCellAnnotation[] subjectCellAnnotations = tableAnnotation.getContentCellAnnotations(row, relation_subobjKey.getSubjectCol());
                        //check if any fact of any candidate subject TCellAnnotation mentions ca
                        //note that we do not need to fix the candidate subject TCellAnnotation at this stage.
                        //because another message should have been sent to the cell that corresponds to the subject cell
                        //and that ensures the subject's cell's annotation will be dealt with separately
                        for (TCellAnnotation subjectCellAnnotation : subjectCellAnnotations) {
                            for (Attribute fact : subjectCellAnnotation.getAnnotation().getAttributes()) {
                                if (fact.getValue().equals(ca.getAnnotation().getId())) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else {  //change message sent by header   todo at this point, we should re-query freebase to fetch more candidates
            List<String> legitHeaderLabels = new ArrayList<>(m.getLabels());
            legitHeaderLabels.retainAll(ca.getAnnotation().getTypeIds());
            if (legitHeaderLabels.size() > 0)
                return true;
        }
        return false;
    }

    //key-index of the TCellAnnotation in the list of candidate CellAnnotations for the cell ranked by their scores
    //value-list of indexes of satisfied messages by this TCellAnnotation.
    private List<Integer> select(Map<Integer, List<Integer>> annotation_satisfies_messages,
                                 List<String> messagePreferencesSorted,
                                 TCellAnnotation[] candidateAnnotationsInCell
                                 ) {
        int bestPreferenceIndex = Integer.MAX_VALUE;
        List<Integer> bestAnnotations = new ArrayList<>();
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

        //prune bestAnnotations, if there are multiple ones chosen by messages, select only the highest scoring ones
        double maxScore=0.0;
        for(int i: bestAnnotations){
            TCellAnnotation ca = candidateAnnotationsInCell[i];
            if(ca.getFinalScore()>maxScore)
                maxScore=ca.getFinalScore();
        }
        Iterator<Integer> it = bestAnnotations.iterator();
        while(it.hasNext()) {
            int index=it.next();
            TCellAnnotation ca = candidateAnnotationsInCell[index];
            if(ca.getFinalScore()!=maxScore)
                it.remove();
        }
        return bestAnnotations;
    }

    //to update cell's annotation, we need to select the one that satisfies most "change" messages sent from different
    //factors. Here we compute different combinations of satisfied "change" messages ranked by preference (see Mulwad's papge
    // on page 12
    /*private List<String> createSortedPreferenceKeys(int totalMessages) {
        Set<String> in = new HashSet<String>(totalMessages);
        for (int i = 0; i < totalMessages; i++) {
            in.add(String.valueOf(i));
        }
        List<String> rs = SubsetGenerator.generateSubsets(in);
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
    }*/

    private List<String> createSortedPreferenceKeys(int totalMessages) {
        Set<Integer> in = new HashSet<>(totalMessages);
        for (int i = 0; i < totalMessages; i++) {
            in.add(i);
        }
        List<String> rs = SubsetGenerator.generateSubsets(in);
        Collections.sort(rs, (o1, o2) -> {
            Integer length1 = o1.length();
            Integer length2 = o2.length();
            int compare = length2.compareTo(length1);
            if (compare == 0) {
                return o1.compareTo(o2);
            } else
                return compare;
        });
        return rs;
    }

    private boolean containsRelation(List<Attribute> facts, List<String> labels) {
        for (Attribute fact : facts) {
            if (labels.contains(fact.getRelationURI()))
                return true;
        }
        return false;
    }
}

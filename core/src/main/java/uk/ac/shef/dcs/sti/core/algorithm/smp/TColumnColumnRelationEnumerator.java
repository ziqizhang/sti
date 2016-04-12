package uk.ac.shef.dcs.sti.core.algorithm.smp;

import javafx.util.Pair;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;
import java.util.List;

/**
 *
 */
public class TColumnColumnRelationEnumerator extends uk.ac.shef.dcs.sti.core.algorithm.tmp.TColumnColumnRelationEnumerator {

    private boolean useSubjectColumn;
    private Collection<Integer> ignoreColumns;

    public TColumnColumnRelationEnumerator(AttributeValueMatcher attributeValueMatcher,
                                           Collection<Integer> ignoreColumns,
                                           boolean useSubjectColumn) {
        super(attributeValueMatcher, null);
        this.ignoreColumns = ignoreColumns;
        this.useSubjectColumn = useSubjectColumn;
    }

    public int runRelationEnumeration(TAnnotation annotations, Table table, int subjectCol) throws STIException {
        if (useSubjectColumn)
            super.generateCellCellRelations(annotations, table, subjectCol);
        else {
            List<Integer> subjectColumnsToConsider = new ArrayList<>();

            for (int c = 0; c < table.getNumCols(); c++) {
                if (!ignoreColumns.contains(c))
                    subjectColumnsToConsider.add(c);
            }

            for (int subjectColumn : subjectColumnsToConsider) {  //choose a column to be subject column (must be NE column)
                if (!table.getColumnHeader(subjectColumn).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                super.generateCellCellRelations(annotations, table, subjectColumn);
            }
        }
        //aggregate overall scores for relations on each column pairs and populate relation annotation object
        aggregate(annotations, table);

        return 0;
    }

    private void aggregate(
            TAnnotation tableAnnotation, Table table) {

        List<RelationColumns> processed = new ArrayList<>();
        for (Map.Entry<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>> e :
                tableAnnotation.getCellcellRelations().entrySet()) {

            //firstly, check the directional relation
            RelationColumns relationColumns = e.getKey(); //key indicating the directional relationship (subject col, object col)
            if (processed.contains(relationColumns))
                continue;
            //key: relationURI, value: votes and score
            Map<String, Pair<Integer, Double>> votes = new HashMap<>();

            processed.add(relationColumns);
            //map object where key=row id, value=collection of binary relations between the sub col and obj col on this row
            Map<Integer, List<TCellCellRelationAnotation>> relationsOnRows = e.getValue();
            //among all relations that apply to the direction subcol-objcol, collect votes
            collectVotes(relationsOnRows, votes);
            List<RelationDataTuple> winner = selectWinner(votes, relationColumns);   //the highest scoring relation in the direction of subject-object

            //next, reverse the relation direction
            votes.clear();
            RelationColumns reverseRelationColumns =
                    new RelationColumns(relationColumns.getObjectCol(), relationColumns.getSubjectCol());
            relationsOnRows = tableAnnotation.getCellcellRelations().get(reverseRelationColumns);
            if (relationsOnRows != null) {
                processed.add(reverseRelationColumns);
                collectVotes(relationsOnRows, votes);
                List<RelationDataTuple> winnerReverseDirection = selectWinner(votes, reverseRelationColumns);   //the highest scoring relation in the direction of object-subject relation

                createHeaderBinaryRelationAnnotations(winner, winnerReverseDirection, tableAnnotation, table);
            } else {//no relation from reverse direction,
                createHeaderBinaryRelationAnnotations(winner, null, tableAnnotation, table);
            }
        }
    }

    /**
     * @param relations key:row index; value: list of relations between the two columns
     * @param votes     key: relation uri; value-key: votes, value-value: score
     */
    private void collectVotes(Map<Integer, List<TCellCellRelationAnotation>> relations,
                              Map<String, Pair<Integer, Double>> votes
    ) {
        for (List<TCellCellRelationAnotation> candidatesOnRow : relations.values()) {        //go thru each row
            Set<String> distinctRelations = new HashSet<>();
            for (TCellCellRelationAnotation candidate : candidatesOnRow) { //go thru each candidate of each row
                String relationCandidate = candidate.getRelationURI();
                distinctRelations.add(relationCandidate);
            }

            for (String relation : distinctRelations) { //go thru each candidate of each row
                double maxScore = 0.0;
                for (TCellCellRelationAnotation candidate : candidatesOnRow) {
                    if (candidate.getRelationURI().equals(relation) && candidate.getWinningAttributeMatchScore() > maxScore)
                        maxScore = candidate.getWinningAttributeMatchScore();
                }

                Pair<Integer, Double> votesAndScore = votes.get(relation); //let's record both votes and scores. so when there is a tie at votes, we resort to scores
                if (votesAndScore == null) {
                    votesAndScore = new Pair<>(0, 0.0);
                } else {
                    votesAndScore = new Pair<>(votesAndScore.getKey() + 1,
                            votesAndScore.getValue() + maxScore);
                }

                votes.put(relation, votesAndScore);
            }
        }
    }

    private List<RelationDataTuple> selectWinner(Map<String, Pair<Integer, Double>> votes,
                                                 RelationColumns relationColumnsKey) {
        List<RelationDataTuple> out = new ArrayList<>();
        for (Map.Entry<String, Pair<Integer, Double>> e : votes.entrySet()) {
            RelationDataTuple rdt = new RelationDataTuple();
            rdt.relationString = e.getKey();
            rdt.relationColumns = relationColumnsKey;
            rdt.votes = e.getValue().getKey();
            rdt.score = e.getValue().getValue();
            out.add(rdt);
        }
        Collections.sort(out);
        int maxVote = out.get(0).votes;
        double maxScore = out.get(0).score;
        Iterator<RelationDataTuple> it = out.iterator();
        while (it.hasNext()) {
            RelationDataTuple rdt = it.next();
            if (rdt.votes < maxVote)
                it.remove();
            else if (rdt.votes == maxVote && rdt.score < maxScore)
                it.remove();
        }
        return out;
    }


    /**
     * need to resolve conflicts. between two directions (sub-ob, or ob-sub, we must choose only 1)
     *
     * @param winner
     * @param winnerReverseDirection
     * @param tableAnnotation
     * @param table
     */
    private void createHeaderBinaryRelationAnnotations(
            List<RelationDataTuple> winner,
            List<RelationDataTuple> winnerReverseDirection,
            TAnnotation tableAnnotation,
            Table table) {
        if (winnerReverseDirection != null)
            winner.addAll(winnerReverseDirection);
        Collections.sort(winner);

        RelationDataTuple finalWinner = winner.get(0);
        int maxVote = finalWinner.votes;
        double maxScore = finalWinner.score;
        RelationColumns relationColumns = finalWinner.relationColumns;
        int countNonEmptyRows = 0;
        for (int r = 0; r < tableAnnotation.getRows(); r++) {
            TCell c1 = table.getContentCell(r, relationColumns.getSubjectCol());
            TCell c2 = table.getContentCell(r, relationColumns.getObjectCol());
            if (!c1.getType().equals(DataTypeClassifier.DataType.EMPTY) &&
                    !c1.getType().equals(DataTypeClassifier.DataType.EMPTY))
                countNonEmptyRows++;
        }

        for (RelationDataTuple rdt : winner) {
            if (rdt.votes == maxVote && rdt.score == maxScore &&
                    rdt.relationColumns.getSubjectCol() == relationColumns.getSubjectCol() &&
                    rdt.relationColumns.getObjectCol() == relationColumns.getObjectCol()) {
                TColumnColumnRelationAnnotation hbr = new TColumnColumnRelationAnnotation(relationColumns,
                        rdt.relationString,
                        rdt.relationString,
                        (double) maxVote / countNonEmptyRows);
                tableAnnotation.addColumnColumnRelation(hbr);

                //add supporting rows
                Map<Integer, List<TCellCellRelationAnotation>>
                        cellcellRelations = tableAnnotation.getRelationAnnotationsBetween(rdt.relationColumns.getSubjectCol(), rdt.relationColumns.getObjectCol());
                for (Map.Entry<Integer, List<TCellCellRelationAnotation>> e : cellcellRelations.entrySet()) {
                    for (TCellCellRelationAnotation cbr : e.getValue()) {
                        if (hbr.getRelationURI().equals(cbr.getRelationURI())) {
                            hbr.addSupportingRow(e.getKey());
                            break;
                        }
                    }
                }

            } else {
                break;
            }
        }


    }

    private class RelationDataTuple implements Comparable<RelationDataTuple> {
        protected String relationString;
        protected RelationColumns relationColumns;
        protected int votes;
        protected double score;

        @Override
        public int compareTo(RelationDataTuple o) {
            int compare = Integer.valueOf(o.votes).compareTo(votes);
            if (compare == 0) {
                return Double.valueOf(o.score).compareTo(score);
            }
            return compare;
        }

        public String toString() {
            return relationString + "," + votes + "," + score;
        }
    }
}

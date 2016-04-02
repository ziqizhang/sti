package uk.ac.shef.dcs.sti.algorithm.tm;

import uk.ac.shef.dcs.sti.rep.TCellAnnotation;
import uk.ac.shef.dcs.sti.rep.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.rep.TAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 27/02/14
 * Time: 09:52
 * To change this template use File | Settings | File Templates.
 */
class TColumnHeaderAnnotationUpdater {

    /**
     * given a cell annotation, and existing TColumnHeaderAnnotations on the column,
     * go through the types (clazz) of that cell annotation, select the ones that are
     * not included in the existing TColumnHeaderAnnotations
     *
     * @param ca
     * @param column
     * @param table
     * @param existingColumnClazzAnnotations
     * @return
     */
    public static List<TColumnHeaderAnnotation> selectNew(TCellAnnotation ca, int column,
                                                          Table table,
                                                          Collection<TColumnHeaderAnnotation> existingColumnClazzAnnotations
    ) {

        List<Clazz> types = ca.getAnnotation().getTypes();

        List<TColumnHeaderAnnotation> selected = new ArrayList<>();
        for (int index = 0; index < types.size(); index++) {
            boolean found = false;
            Clazz type = types.get(index);
            for (TColumnHeaderAnnotation ha : existingColumnClazzAnnotations) {
                if (type.equals(ha.getAnnotation())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                TColumnHeaderAnnotation ha = new TColumnHeaderAnnotation(table.getColumnHeader(column).getHeaderText(),
                        type, 0.0);
                selected.add(ha);
            }
        }
        return selected;
    }

    /**
     * Used after preliminary disamb, to update candidate column clazz annotations on the column
     * @param updatedRows
     * @param column
     * @param totalRows
     * @param candidateColumnClazzAnnotations
     * @param table
     * @param tableAnnotations
     * @param clazzScorer
     * @return
     */
    public static TColumnHeaderAnnotation[] updateColumnClazzAnnotationScores(Collection<Integer> updatedRows,
                                                                              int column,
                                                                              int totalRows,
                                                                              Collection<TColumnHeaderAnnotation> candidateColumnClazzAnnotations,
                                                                              Table table,
                                                                              TAnnotation tableAnnotations,
                                                                              ClazzScorer clazzScorer) {
        //for the candidate column clazz annotations compute CC score
        candidateColumnClazzAnnotations = clazzScorer.computeCCScore(candidateColumnClazzAnnotations,table, column);

        for (int row : updatedRows) {
            List<TCellAnnotation> winningEntities = tableAnnotations.getWinningContentCellAnnotation(row, column);
            Set<String> votedClazzIdsByThisCell = new HashSet<>();
            for (TCellAnnotation ca : winningEntities) {
                //go thru each candidate column clazz annotation, check if this winning entity has a type that is this clazz
                for (TColumnHeaderAnnotation ha : candidateColumnClazzAnnotations) {
                    if (ca.getAnnotation().getTypes().contains(ha.getAnnotation())) {
                        ha.addSupportingRow(row);
                        if (!votedClazzIdsByThisCell.contains(ha.getAnnotation().getId())) {
                            //update the CE score elements for this column clazz annotation
                            double sum_votes = ha.getScoreElements().get(TColumnHeaderAnnotation.SUM_CELL_VOTE);
                            sum_votes++;
                            ha.getScoreElements().put(TColumnHeaderAnnotation.SUM_CELL_VOTE, sum_votes);

                            double sum_ce = ha.getScoreElements().get(TColumnHeaderAnnotation.SUM_CE);
                            sum_ce += ca.getFinalScore();
                            ha.getScoreElements().put(TColumnHeaderAnnotation.SUM_CE, sum_ce);
                            votedClazzIdsByThisCell.add(ha.getAnnotation().getId());
                        }
                    } else if (votedClazzIdsByThisCell.contains(ha.getAnnotation().getId())){}

                }
            }
        }

        //finally recompute final scores, because CE scores could have changed
        List<TColumnHeaderAnnotation> revised = new ArrayList<>();
        for (TColumnHeaderAnnotation ha : candidateColumnClazzAnnotations) {
            clazzScorer.computeFinal(ha, totalRows);
            revised.add(ha);
        }

        Collections.sort(revised);
        return revised.toArray(new TColumnHeaderAnnotation[0]);
    }

    public static void add(
            Map<String, String> header_annotation_url_and_label,
            int column,
            Table table,
            TColumnHeaderAnnotation[] existing_header_annotations,
            Set<TColumnHeaderAnnotation> new_header_annotation_placeholders
    ) {

        for (Map.Entry<String, String> e : header_annotation_url_and_label.entrySet()) {
            String url = e.getKey();
            String label = e.getValue();
            boolean found = false;

            for (TColumnHeaderAnnotation ha : existing_header_annotations) {
                if (url.equalsIgnoreCase(ha.getAnnotation().getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                new_header_annotation_placeholders.add(
                        new TColumnHeaderAnnotation(table.getColumnHeader(column).getHeaderText(),
                                new Clazz(url, label), 0.0));
            }
        }
    }

    //results are not sorted!!
    public static void update_by_entity_contribution(Map<String, Double> header_annotation_url_and_max_score,
                                                     int row,
                                                     TColumnHeaderAnnotation[] existing_header_annotations) {
        for (Map.Entry<String, Double> e : header_annotation_url_and_max_score.entrySet()) {
            String type_url = e.getKey();
            Double score = e.getValue();
            for (TColumnHeaderAnnotation ha : existing_header_annotations) {
                if (type_url.equals(ha.getAnnotation().getId())) {
                    ha.addSupportingRow(row);
                    double sum_votes = ha.getScoreElements().get(TColumnHeaderAnnotation.SUM_CELL_VOTE);
                    sum_votes++;
                    ha.getScoreElements().put(TColumnHeaderAnnotation.SUM_CELL_VOTE, sum_votes);
                    double sum_base_score = ha.getScoreElements().get(TColumnHeaderAnnotation.SUM_CE);
                    sum_base_score += score;
                    ha.getScoreElements().put(TColumnHeaderAnnotation.SUM_CE, sum_base_score);
                }
                /* if(ha.getAnnotation().equals("/periodicals/newspaper_circulation_area"))
                p.println(cAnn.getHeaderText()+","+ha.getFinalScore());*/
            }
        }
    }

}

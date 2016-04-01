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
class HeaderAnnotationUpdater {

    public static void add(TCellAnnotation ca, int column,
                           Table table,
                           TColumnHeaderAnnotation[] existing_header_annotations,
                           Set<TColumnHeaderAnnotation> new_header_annotation_placeholders
    ) {

        List<Clazz> types = ca.getAnnotation().getTypes();

        for (int index = 0; index < types.size(); index++) {
            boolean found = false;
            Clazz type = types.get(index);
            for (TColumnHeaderAnnotation ha : existing_header_annotations) {
                if (type.getId().equalsIgnoreCase(ha.getAnnotation().getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                new_header_annotation_placeholders.add(
                        new TColumnHeaderAnnotation(table.getColumnHeader(column).getHeaderText(),
                                type, 0.0));
            }
        }
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
                    double sum_votes = ha.getScoreElements().get(TColumnHeaderAnnotation.SUM_ENTITY_VOTE);
                    sum_votes++;
                    ha.getScoreElements().put(TColumnHeaderAnnotation.SUM_ENTITY_VOTE, sum_votes);
                    double sum_base_score = ha.getScoreElements().get(TColumnHeaderAnnotation.SUM_CE);
                    sum_base_score += score;
                    ha.getScoreElements().put(TColumnHeaderAnnotation.SUM_CE, sum_base_score);
                }
                /* if(ha.getAnnotation().equals("/periodicals/newspaper_circulation_area"))
                p.println(cAnn.getHeaderText()+","+ha.getFinalScore());*/
            }
        }
    }

    public static TColumnHeaderAnnotation[] update_best_entity_contribute(Integer[] rows,
                                                                   int column,
                                                                   int totalRows,
                                                                   TColumnHeaderAnnotation[] existing_header_annotations,
                                                                   Table table,
                                                                   TAnnotation table_annotations,
                                                                   ClazzScorer classification_scorer) {
        Set<TColumnHeaderAnnotation> headers = new HashSet<TColumnHeaderAnnotation>(Arrays.asList(existing_header_annotations));
        headers = classification_scorer.computeCCScore(
                headers, table, column);
        existing_header_annotations = headers.toArray(new TColumnHeaderAnnotation[0]);


        for (int row : rows) {
            List<TCellAnnotation> bestCellAnnotations = table_annotations.getWinningContentCellAnnotation(row, column);
            Set<String> types_already_received_votes_by_cell=new HashSet<String>();
            for (TCellAnnotation ca : bestCellAnnotations) {
                for (TColumnHeaderAnnotation ha : existing_header_annotations) {
                    if (ca.getAnnotation().hasType(ha.getAnnotation().getId())) {
                        ha.addSupportingRow(row);
                        if (!types_already_received_votes_by_cell.contains(ha.getAnnotation().getId())) {
                            double sum_votes = ha.getScoreElements().get(TColumnHeaderAnnotation.SUM_ENTITY_VOTE);
                            sum_votes++;
                            ha.getScoreElements().put(TColumnHeaderAnnotation.SUM_ENTITY_VOTE, sum_votes);

                            double sum_base_score = ha.getScoreElements().get(TColumnHeaderAnnotation.SUM_CE);
                            sum_base_score += ca.getFinalScore();
                            ha.getScoreElements().put(TColumnHeaderAnnotation.SUM_CE, sum_base_score);
                            types_already_received_votes_by_cell.add(ha.getAnnotation().getId());
                        }
                        }else if(types_already_received_votes_by_cell.contains(ha.getAnnotation().getId()))
                            System.out.print(".");
                    }
                    /* if(ha.getAnnotation().equals("/periodicals/newspaper_circulation_area"))
                    p.println(cAnn.getHeaderText()+","+ha.getFinalScore());*/

            }
            //p.close();

        }

        //final update to compute revised typing scores, then sort them
        List<TColumnHeaderAnnotation> resort = new ArrayList<TColumnHeaderAnnotation>();
        for (TColumnHeaderAnnotation ha : existing_header_annotations) {
            classification_scorer.computeFinal(ha, totalRows);
            /* ha.setScoreElements(revised_score_elements);
            ha.setFinalScore(revised_score_elements.get(TColumnHeaderAnnotation.FINAL));*/
            resort.add(ha);
        }

        Collections.sort(resort);
        return resort.toArray(new TColumnHeaderAnnotation[0]);
    }
}

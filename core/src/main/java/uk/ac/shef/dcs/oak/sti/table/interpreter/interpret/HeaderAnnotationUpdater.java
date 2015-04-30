package uk.ac.shef.dcs.oak.sti.table.interpreter.interpret;

import uk.ac.shef.dcs.oak.sti.table.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.sti.table.rep.HeaderAnnotation;
import uk.ac.shef.dcs.oak.sti.table.rep.LTable;
import uk.ac.shef.dcs.oak.sti.table.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.sti.test.TableMinerConstants;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 27/02/14
 * Time: 09:52
 * To change this template use File | Settings | File Templates.
 */
class HeaderAnnotationUpdater {

    public static void add(CellAnnotation ca, int column,
                           LTable table,
                           HeaderAnnotation[] existing_header_annotations,
                           Set<HeaderAnnotation> new_header_annotation_placeholders
    ) {

        List<String[]> types = ca.getAnnotation().getTypes();

        for (int index = 0; index < types.size(); index++) {
            boolean found = false;
            String[] type = types.get(index);
            for (HeaderAnnotation ha : existing_header_annotations) {
                if (type[0].equalsIgnoreCase(ha.getAnnotation_url())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                new_header_annotation_placeholders.add(
                        new HeaderAnnotation(table.getColumnHeader(column).getHeaderText(),
                                type[0], type[1], 0.0));
            }
        }
    }

    public static void add(
            Map<String, String> header_annotation_url_and_label,
            int column,
            LTable table,
            HeaderAnnotation[] existing_header_annotations,
            Set<HeaderAnnotation> new_header_annotation_placeholders
    ) {

        for (Map.Entry<String, String> e : header_annotation_url_and_label.entrySet()) {
            String url = e.getKey();
            String label = e.getValue();
            boolean found = false;

            for (HeaderAnnotation ha : existing_header_annotations) {
                if (url.equalsIgnoreCase(ha.getAnnotation_url())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                new_header_annotation_placeholders.add(
                        new HeaderAnnotation(table.getColumnHeader(column).getHeaderText(),
                                url, label, 0.0));
            }
        }
    }

    //results are not sorted!!
    public static void update_by_entity_contribution(Map<String, Double> header_annotation_url_and_max_score,
                                                     int row,
                                                     HeaderAnnotation[] existing_header_annotations) {
        for (Map.Entry<String, Double> e : header_annotation_url_and_max_score.entrySet()) {
            String type_url = e.getKey();
            Double score = e.getValue();
            for (HeaderAnnotation ha : existing_header_annotations) {
                if (type_url.equals(ha.getAnnotation_url())) {
                    ha.addSupportingRow(row);
                    double sum_votes = ha.getScoreElements().get(HeaderAnnotation.SUM_ENTITY_VOTE);
                    sum_votes++;
                    ha.getScoreElements().put(HeaderAnnotation.SUM_ENTITY_VOTE, sum_votes);
                    double sum_base_score = ha.getScoreElements().get(HeaderAnnotation.SUM_ENTITY_DISAMB);
                    sum_base_score += score;
                    ha.getScoreElements().put(HeaderAnnotation.SUM_ENTITY_DISAMB, sum_base_score);
                }
                /* if(ha.getAnnotation().equals("/periodicals/newspaper_circulation_area"))
                p.println(cAnn.getTerm()+","+ha.getFinalScore());*/
            }
        }
    }

    public static HeaderAnnotation[] update_best_entity_contribute(Integer[] rows,
                                                                   int column,
                                                                   int totalRows,
                                                                   HeaderAnnotation[] existing_header_annotations,
                                                                   LTable table,
                                                                   LTableAnnotation table_annotations,
                                                                   ClassificationScorer classification_scorer) {
        Set<HeaderAnnotation> headers = new HashSet<HeaderAnnotation>(Arrays.asList(existing_header_annotations));
        headers = classification_scorer.score_context(
                headers, table, column, false);
        existing_header_annotations = headers.toArray(new HeaderAnnotation[0]);


        for (int row : rows) {
            List<CellAnnotation> bestCellAnnotations = table_annotations.getBestContentCellAnnotations(row, column);
            Set<String> types_already_received_votes_by_cell=new HashSet<String>();
            for (CellAnnotation ca : bestCellAnnotations) {
                for (HeaderAnnotation ha : existing_header_annotations) {
                    if (ca.getAnnotation().hasTypeId(ha.getAnnotation_url())) {
                        ha.addSupportingRow(row);
                        if (TableMinerConstants.BEST_CANDIDATE_CONTRIBUTE_COUNT_ONLY_ONCE
                                &&!types_already_received_votes_by_cell.contains(ha.getAnnotation_url())) {
                            double sum_votes = ha.getScoreElements().get(HeaderAnnotation.SUM_ENTITY_VOTE);
                            sum_votes++;
                            ha.getScoreElements().put(HeaderAnnotation.SUM_ENTITY_VOTE, sum_votes);

                            double sum_base_score = ha.getScoreElements().get(HeaderAnnotation.SUM_ENTITY_DISAMB);
                            sum_base_score += ca.getFinalScore();
                            ha.getScoreElements().put(HeaderAnnotation.SUM_ENTITY_DISAMB, sum_base_score);
                            types_already_received_votes_by_cell.add(ha.getAnnotation_url());
                        }else if(!TableMinerConstants.BEST_CANDIDATE_CONTRIBUTE_COUNT_ONLY_ONCE
                                ){
                            double sum_votes = ha.getScoreElements().get(HeaderAnnotation.SUM_ENTITY_VOTE);
                            sum_votes++;
                            ha.getScoreElements().put(HeaderAnnotation.SUM_ENTITY_VOTE, sum_votes);

                            double sum_base_score = ha.getScoreElements().get(HeaderAnnotation.SUM_ENTITY_DISAMB);
                            sum_base_score += ca.getFinalScore();
                            ha.getScoreElements().put(HeaderAnnotation.SUM_ENTITY_DISAMB, sum_base_score);
                            types_already_received_votes_by_cell.add(ha.getAnnotation_url());
                        }else if(types_already_received_votes_by_cell.contains(ha.getAnnotation_url()))
                            System.out.print(".");
                    }
                    /* if(ha.getAnnotation().equals("/periodicals/newspaper_circulation_area"))
                    p.println(cAnn.getTerm()+","+ha.getFinalScore());*/
                }
            }
            //p.close();

        }

        //final update to compute revised typing scores, then sort them
        List<HeaderAnnotation> resort = new ArrayList<HeaderAnnotation>();
        for (HeaderAnnotation ha : existing_header_annotations) {
            classification_scorer.compute_final_score(ha, totalRows);
            /* ha.setScoreElements(revised_score_elements);
            ha.setFinalScore(revised_score_elements.get(HeaderAnnotation.FINAL));*/
            resort.add(ha);
        }

        Collections.sort(resort);
        return resort.toArray(new HeaderAnnotation[0]);
    }
}

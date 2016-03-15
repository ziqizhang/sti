package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.HeaderAnnotation;
import uk.ac.shef.dcs.sti.rep.LTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 24/01/14
 * Time: 13:47
 * To change this template use File | Settings | File Templates.
 */
public interface ClassificationScorer {
    //input: key is table row index; value is list of candidate entities and their disambiguation scores for that row
    //output: a map representing the state of the solution
    Map<String,Double> compute_final_score(HeaderAnnotation ha, int tableRowsTotal);
    //intput: list of candidates and their disamb scores on the current row;
    Set<HeaderAnnotation> score(List<Pair<Entity, Map<String, Double>>> input,
                                Set<HeaderAnnotation> headerAnnotations_prev,
                                LTable table,
                                List<Integer> rows, int column);

    Set<HeaderAnnotation> score_context(Set<HeaderAnnotation> candidates, LTable table, int column, boolean overwrite);

    double score_domain_consensus(HeaderAnnotation ha, List<String> domain_representation);
}

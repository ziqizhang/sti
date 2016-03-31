package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;

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
public interface TColumnClassifier {
    //input: key is table row index; value is list of candidate entities and their disambiguation scores for that row
    //output: a map representing the state of the solution
    Map<String,Double> computeFinal(TColumnHeaderAnnotation ha, int tableRowsTotal);
    //intput: list of entities and their preliminary disamb scores on the current row;
    Set<TColumnHeaderAnnotation> score(List<Pair<Entity, Map<String, Double>>> input,
                                Set<TColumnHeaderAnnotation> headerAnnotationCandidates,
                                Table table,
                                List<Integer> rows, int column);

    Set<TColumnHeaderAnnotation> computeCCScore(Set<TColumnHeaderAnnotation> candidates, Table table, int column);

    double computeDC(TColumnHeaderAnnotation ha, List<String> domain_representation);
}

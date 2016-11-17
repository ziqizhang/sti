package uk.ac.shef.dcs.sti.core.scorer;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 24/01/14
 * Time: 13:47
 * To change this template use File | Settings | File Templates.
 */
public interface ClazzScorer {


    //input: key is table row index; value is list of candidate entities and their disambiguation scores for that row
    //output: a map representing the state of the solution
    Map<String,Double> computeFinal(TColumnHeaderAnnotation ha, int tableRowsTotal);
    //intput: list of entities and their preliminary disamb scores on the current row;
    List<TColumnHeaderAnnotation> computeElementScores(List<Pair<Entity, Map<String, Double>>> input,
                                                      Collection<TColumnHeaderAnnotation> headerAnnotationCandidates,
                                                      Table table,
                                                      List<Integer> rows, int column) throws STIException;

    List<TColumnHeaderAnnotation> computeCEScore(List<Pair<Entity, Map<String, Double>>> entities,
                                                           Collection<TColumnHeaderAnnotation> existingHeaderAnnotations,
                                                           Table table,
                                                           int row, int column) throws STIException;

    /**
     * compute CC scores for column clazz annotation candidates, ONLY IF the CC score is not yet computed
     * @param candidates
     * @param table
     * @param column
     * @return
     */
    List<TColumnHeaderAnnotation> computeCCScore(Collection<TColumnHeaderAnnotation> candidates, Table table, int column) throws STIException;

    double computeDC(TColumnHeaderAnnotation ha, List<String> domain_representation) throws STIException;


}

package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.rep.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
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
                                                      List<Integer> rows, int column);

    /**
     * compute CC scores for column clazz annotation candidates, ONLY IF the CC score is not yet computed
     * @param candidates
     * @param table
     * @param column
     * @return
     */
    List<TColumnHeaderAnnotation> computeCCScore(Collection<TColumnHeaderAnnotation> candidates, Table table, int column);

    double computeDC(TColumnHeaderAnnotation ha, List<String> domain_representation);


}

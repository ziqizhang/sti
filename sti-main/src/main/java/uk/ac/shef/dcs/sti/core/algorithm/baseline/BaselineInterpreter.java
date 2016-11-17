package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LiteralColumnTagger;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.RELATIONENUMERATION;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.TColumnColumnRelationEnumerator;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 25/02/14
 * Time: 15:52
 * To change this template use File | Settings | File Templates.
 */
public class BaselineInterpreter extends SemanticTableInterpreter {
    private static final Logger LOG = Logger.getLogger(BaselineInterpreter.class.getName());
    private SubjectColumnDetector subjectColumnDetector;
    private TCellDisambiguator disambiguator;
    private TColumnClassifier columnClassifier;
    private TColumnColumnRelationEnumerator relationEnumerator;
    private LiteralColumnTagger literalColumnTagger;


    public BaselineInterpreter(SubjectColumnDetector subjectColumnDetector,
                               TCellDisambiguator disambiguator,
                               TColumnClassifier columnClassifier,
                               TColumnColumnRelationEnumerator relationEnumerator,
                               LiteralColumnTagger literalColumnTagger,
                               int[] ignoreColumns, int[] mustdoColumns) {
        super(ignoreColumns, mustdoColumns);
        this.subjectColumnDetector = subjectColumnDetector;
        this.disambiguator = disambiguator;
        this.columnClassifier = columnClassifier;
        this.relationEnumerator = relationEnumerator;
        this.literalColumnTagger = literalColumnTagger;
    }

    public TAnnotation start(Table table, Constraints constraints) throws STIException {
      return start(table, true);
    }

    public TAnnotation start(Table table, boolean relationLearning) throws STIException {
        try {
            LOG.info(">\t Detecting subject column...");
            int[] ignoreColumnsArray = new int[getIgnoreColumns().size()];

            int index = 0;
            for (Integer i : getIgnoreColumns()) {
                ignoreColumnsArray[index] = i;
                index++;
            }
            //1. find the main subject column of this table
            List<Pair<Integer, Pair<Double, Boolean>>> subjectColumnScores =
                    subjectColumnDetector.compute(table, ignoreColumnsArray);

            TAnnotation tableAnnotations = new TAnnotation(table.getNumRows(), table.getNumCols());
            List<Integer> annotatedColumns = new ArrayList<>();
            Map<Integer, List<Pair<Entity, Map<String, Double>>>> disambResults;
            for (int col = 0; col < table.getNumCols(); col++) {
                LOG.info(">\t Cell Disambiguation for column=" + col);
                if (getMustdoColumns().contains(col)) {
                    LOG.info("\t>> Column=(compulsory)" + col);
                    disambResults = disambiguator.disambiguate(table, tableAnnotations, col);
                    annotatedColumns.add(col);
                } else {
                    if (getIgnoreColumns().contains(col)) continue;
                    if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                        continue;

                    annotatedColumns.add(col);
                    LOG.info("\t>> Column=" + col);
                    disambResults = disambiguator.disambiguate(table, tableAnnotations, col);
                }

                LOG.info(">\t Column classification for column=" + col);
                columnClassifier.classify(disambResults, table, tableAnnotations, col);
                LOG.info(">\t Update cell annotatation based on column class for colun=" + col);
                disambiguator.revise(tableAnnotations, table, disambResults, col);
            }

            if (relationLearning) {
                new RELATIONENUMERATION().enumerate(subjectColumnScores,
                        getIgnoreColumns(), relationEnumerator,
                        tableAnnotations, table,
                        annotatedColumns, null);
                //4. consolidation-for columns that have relation with main subject column, if the column is
                // entity column, do column typing and disambiguation; otherwise, simply create header annotation
                LOG.info("\t\t>> Annotate literal-columns in relation with main column");
                literalColumnTagger.annotate(table, tableAnnotations, annotatedColumns.toArray(new Integer[0]));
            }
            return tableAnnotations;
        } catch (Exception e) {
            throw new STIException(e);
        }
    }

}

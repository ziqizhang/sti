package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.extension.constraints.Classification;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TMPOdalicInterpreter extends SemanticTableInterpreter {
  
  private SubjectColumnDetector subjectColumnDetector;
  private LEARNING learning;
  private LiteralColumnTagger literalColumnTagger;
  private TColumnColumnRelationEnumerator relationEnumerator;
  private UPDATE update;
  
  private static final Logger LOG = LoggerFactory.getLogger(TMPOdalicInterpreter.class.getName());
  
  public TMPOdalicInterpreter(SubjectColumnDetector subjectColumnDetector,
      LEARNING learning, UPDATE update,
      TColumnColumnRelationEnumerator relationEnumerator,
      LiteralColumnTagger literalColumnTagger) {
    super(new int[0], new int[0]);
    this.subjectColumnDetector = subjectColumnDetector;
    this.learning = learning;
    this.literalColumnTagger = literalColumnTagger;
    this.relationEnumerator = relationEnumerator;
    this.update = update;
  }
  
  public TAnnotation start(Table table, boolean relationLearning) throws STIException {
    return start(table, new Constraints());
  }
  
  public TAnnotation start(Table table, Constraints constraints) throws STIException {
    Preconditions.checkNotNull(constraints);
    
    Set<Integer> ignoreCols = constraints.getColumnIgnores().stream()
        .map(e -> e.getPosition().getIndex()).collect(Collectors.toSet());
    
    for (Classification classification : constraints.getClassifications()) {
      // if the chosen classification is empty, we also want to ignore this column
      if (classification.getAnnotation().getChosen().isEmpty()) {
        ignoreCols.add(classification.getPosition().getIndex());
      }
    }
    
    setIgnoreColumns(ignoreCols);
    
    int[] ignoreColumnsArray = getIgnoreColumns().stream().mapToInt(e -> e.intValue()).sorted().toArray();
    literalColumnTagger.setIgnoreColumns(ignoreColumnsArray);
    
    try {
      TAnnotation tableAnnotations = new TAnnotation(table.getNumRows(), table.getNumCols());
      
      // 1. find the main subject column of this table
      LOG.info(">\t PHASE: Detecting subject column ...");
      List<Pair<Integer, Pair<Double, Boolean>>> subjectColumnScores = subjectColumnDetector
          .compute(table, constraints.getSubjectColumnPosition(), ignoreColumnsArray);
      tableAnnotations.setSubjectColumn(subjectColumnScores.get(0).getKey());
      
      List<Integer> annotatedColumns = new ArrayList<>();
      LOG.info(">\t PHASE: LEARNING ...");
      for (int col = 0; col < table.getNumCols(); col++) {
                if (isCompulsoryColumn(col)) {
                    LOG.info("\t>> Column=(compulsory)" + col);
                    annotatedColumns.add(col);
                    learning.learn(table, tableAnnotations, col, constraints);
                } else {
                    if (getIgnoreColumns().contains(col)) continue;
                    if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                        continue;
                /*if (table.getColumnHeader(col).getFeature().isAcronymColumn())
                    continue;*/
                    annotatedColumns.add(col);
                    
                    //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                    LOG.info("\t>> Column=" + col);
                    learning.learn(table, tableAnnotations, col, constraints);
                }
      }
      
      if (update != null) {
        LOG.info(">\t PHASE: UPDATE phase ...");
        update.update(annotatedColumns, table, tableAnnotations, constraints);
      }
      
      LOG.info("\t> PHASE: RELATION ENUMERATION ...");
      new RELATIONENUMERATION().enumerate(subjectColumnScores, getIgnoreColumns(), relationEnumerator,
          tableAnnotations, table, annotatedColumns, update, constraints);
      
                // 4. consolidation - for columns that have relation with main subject column, if the column is
                // entity column, do column typing and disambiguation; otherwise, simply create header annotation
                LOG.info("\t\t>> Annotate literal-columns in relation with main column");
                literalColumnTagger.annotate(table, tableAnnotations, annotatedColumns.toArray(new Integer[0]));
      
      return tableAnnotations;
    } catch (Exception e) {
      throw new STIException(e);
    }
  }
}

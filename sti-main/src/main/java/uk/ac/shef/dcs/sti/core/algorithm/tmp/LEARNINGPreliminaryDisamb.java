package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;
import java.util.List;

/**
 */
public class LEARNINGPreliminaryDisamb {

    private static final Logger LOG = LoggerFactory.getLogger(LEARNINGPreliminaryDisamb.class.getName());
    private TCellDisambiguator disambiguator;
    private KBProxy kbSearch;
    private TColumnClassifier classifier;

    public LEARNINGPreliminaryDisamb(KBProxy kbSearch,
                                     TCellDisambiguator disambiguator,
                                     TColumnClassifier classifier) {
        this.kbSearch = kbSearch;
        this.disambiguator = disambiguator;
        this.classifier = classifier;
    }

    public void runPreliminaryDisamb(
            int stopPointByPreColumnClassifier,
            List<List<Integer>> ranking,
            Table table,
            TAnnotation tableAnnotation,
            int column,
            Constraints constraints,
            Integer... skipRows) throws KBProxyException, STIException {

        LOG.info("\t>> (LEARNING) Preliminary Disambiguation begins");
        List<TColumnHeaderAnnotation> winningColumnClazz = tableAnnotation.getWinningHeaderAnnotations(column);
        Set<String> winningColumnClazzIds = new HashSet<>();
        for (TColumnHeaderAnnotation ha : winningColumnClazz)
            winningColumnClazzIds.add(ha.getAnnotation().getId());

        //for those cells already processed by pre column classification, update their cell annotations
        LOG.info("\t\t>> re-annotate cells involved in cold start disambiguation");
        reselect(tableAnnotation, stopPointByPreColumnClassifier, ranking, winningColumnClazzIds, column);

        //for remaining...
        LOG.info("\t\t>> constrained cell disambiguation for the rest cells in this column");
        int end = ranking.size();

        List<Integer> updated = new ArrayList<>();
        for (int bi = stopPointByPreColumnClassifier; bi < end; bi++) {
            List<Integer> rows = ranking.get(bi);

            boolean skip = false;
            for (int i : skipRows) {
                if (rows.contains(i)) {
                    skip = true;
                    break;
                }
            }
            if (skip)
                continue;

            TCell sample = table.getContentCell(rows.get(0), column);
            if (sample.getText().length() < 2) {
                LOG.debug("\t\t>>> short text cell skipped: " + rows + "," + column + " " + sample.getText());
                continue;
            }

            List<Pair<Entity, Map<String, Double>>> entity_and_scoreMap =
                    constrainedDisambiguate(sample,
                            table,
                            winningColumnClazzIds,
                            rows, column, ranking.size(),
                            constraints
                    );

            if (entity_and_scoreMap.size() > 0) {
                disambiguator.addCellAnnotation(table, tableAnnotation, rows, column,
                        entity_and_scoreMap);
                updated.addAll(rows);
            }
        }

        LOG.info("\t\t>> constrained cell disambiguation complete " + updated.size() + "/"+ranking.size()+" rows");
        LOG.info("\t\t>> reset candidate column class annotations");
        classifier.updateColumnClazz(updated, column, tableAnnotation, table,false);

    }

    //for those cells already processed in preliminary column classification,
    //preliminary disamb simply reselects entities whose type overlap with winning column clazz annotation
    private void reselect(TAnnotation tableAnnotation,
                          int stopPointByPreColumnClassifier,
                          List<List<Integer>> cellBlockRanking,
                          Collection<String> winningClazzIds,
                          int column) {
        TColumnHeaderAnnotation[] headers = tableAnnotation.getHeaderAnnotation(column);
        for (int index = 0; index < stopPointByPreColumnClassifier; index++) {
            List<Integer> cellBlock = cellBlockRanking.get(index);
            for (int row : cellBlock) {
                TCellAnnotation[] cellAnnotations =
                        tableAnnotation.getContentCellAnnotations(row, column);
                TCellAnnotation[] revised =
                        disambiguator.reselect(cellAnnotations, winningClazzIds);
                if (revised.length != 0)
                    tableAnnotation.setContentCellAnnotations(row, column, revised);

                //now update supporting rows for the elected column clazz
                if (headers != null) {
                    for (TColumnHeaderAnnotation ha : headers) {
                        for (TCellAnnotation tca : revised) {
                            if (tca.getAnnotation().getTypes().contains(ha.getAnnotation())) {
                                ha.addSupportingRow(row);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }


    //search candidates for the cell;
    //computeElementScores candidates for the cell;
    //create annotation and update supportin header and header computeElementScores (depending on the two params updateHeader_blah
    private List<Pair<Entity, Map<String, Double>>> constrainedDisambiguate(TCell tcc,
                                                                            Table table,
                                                                            Set<String> winningColumnClazz,
                                                                            List<Integer> rowBlock,
                                                                            int column,
                                                                            int totalRowBlocks,
                                                                            Constraints constraints) throws KBProxyException {
        List<Pair<Entity, Map<String, Double>>> entity_and_scoreMap;

        List<Entity> candidates = constraints.getDisambChosenForCell(column, rowBlock.get(0));

        if (candidates.isEmpty()) {
          candidates = kbSearch.findEntityCandidatesOfTypes(tcc.getText(), winningColumnClazz.toArray(new String[0]));
        }

        if (candidates != null && candidates.size() != 0) {
        } else
            candidates = kbSearch.findEntityCandidatesOfTypes(tcc.getText());

        //now each candidate is given scores
        entity_and_scoreMap =
                disambiguator.constrainedDisambiguate
                        (candidates, table, rowBlock, column,totalRowBlocks, true);

        return entity_and_scoreMap;
    }
}

package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.*;

import java.util.*;

/**
 */
public class LEARNINGPreliminaryDisamb {

    private static final Logger LOG = Logger.getLogger(LEARNINGPreliminaryDisamb.class.getName());
    private TCellDisambiguator disambiguator;
    private KBSearch kbSearch;
    private ClazzScorer clazzScorer;

    public LEARNINGPreliminaryDisamb(KBSearch kbSearch,
                                     TCellDisambiguator disambiguator,
                                     ClazzScorer clazzScorer) {
        this.kbSearch = kbSearch;
        this.disambiguator = disambiguator;
        this.clazzScorer = clazzScorer;
    }

    public void runPreliminaryDisamb(
            int stopPointByPreColumnClassifier,
            List<List<Integer>> ranking,
            Table table,
            TAnnotation tableAnnotation,
            int column,
            Integer... skipRows) throws KBSearchException {

        LOG.info("\t>> (LEARNING) Preliminary Disambiguation begins");
        List<TColumnHeaderAnnotation> winningColumnClazz = tableAnnotation.getWinningHeaderAnnotations(column);
        Set<String> winningColumnClazzIds = new HashSet<>();
        for (TColumnHeaderAnnotation ha : winningColumnClazz)
            winningColumnClazzIds.add(ha.getAnnotation().getId());

        //for those cells already processed by pre column classification, update their cell annotations
        LOG.info("\t\t>> re-annotate cells involved in cold start disambiguation");
        reselect(tableAnnotation, stopPointByPreColumnClassifier, ranking, winningColumnClazzIds, column);

        //for remaining...
        LOG.info("\t\t>> constrained cell disambiguation");
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
                            rows, column
                    );

            if (entity_and_scoreMap.size() > 0) {
                TMPInterpreter.addCellAnnotation(table, tableAnnotation, rows, column,
                        entity_and_scoreMap);
                updated.addAll(rows);
            }
        }

        LOG.info("\t\t>> constrained cell disambiguation complete " + updated.size() + " rows");
        LOG.info("\t\t>> reset candidate column class annotations");
        TMPInterpreter.updateColumnClazz(updated, column, tableAnnotation, table,clazzScorer);

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
                                                                            int column) throws KBSearchException {
        List<Pair<Entity, Map<String, Double>>> entity_and_scoreMap;

        List<Entity> candidates = kbSearch.findEntityCandidatesOfTypes(tcc.getText(), winningColumnClazz.toArray(new String[0]));
        if (candidates != null && candidates.size() != 0) {
        } else
            candidates = kbSearch.findEntityCandidatesOfTypes(tcc.getText());

        //now each candidate is given scores
        entity_and_scoreMap =
                disambiguator.constrainedDisambiguate
                        (candidates, table, rowBlock, column, true);

        return entity_and_scoreMap;
    }
}

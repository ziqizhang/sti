package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.scorer.EntityScorer;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 */
public class TCellDisambiguator {

    private KBSearch kbSearch;
    private EntityScorer disambScorer;
    private static final Logger LOG = Logger.getLogger(TCellDisambiguator.class.getName());

    public TCellDisambiguator(KBSearch kbSearch, EntityScorer disambScorer) {
        this.kbSearch = kbSearch;
        this.disambScorer = disambScorer;
    }
    //this method runs cold start disambiguation
    public List<Pair<Entity, Map<String, Double>>> coldstartDisambiguate(List<Entity> candidates, Table table,
                                                                         List<Integer> entity_rows, int entity_column
    ) throws KBSearchException {
        LOG.info("\t\t>> (cold start disamb), candidates=" + candidates.size());
        return disambiguate(candidates, table,entity_rows,entity_column);

    }

    //reselect cell entities for this cell ensuring their types are contained in the winning clazz for the column
    public TCellAnnotation[] reselect(
            TCellAnnotation[] existingCellAnnotations,
            Collection<String> winningClazzIdsForColumn) {
        List<TCellAnnotation> selected = new ArrayList<>();
        for(TCellAnnotation tca: existingCellAnnotations){
            int overlap = CollectionUtils.intersection(tca.getAnnotation().getTypeIds(),
                    winningClazzIdsForColumn).size();
            if(overlap>0)
                selected.add(tca);
        }

        return selected.toArray(new TCellAnnotation[0]);
    }

    public List<Pair<Entity, Map<String, Double>>> constrainedDisambiguate(
            List<Entity> candidates,
            Table table,
            List<Integer> rowBlock,
            int column,
            int totalRowBlocks,
            boolean isLEARNINGPhase
    ) throws KBSearchException {
        TCell sample_tcc = table.getContentCell(rowBlock.get(0), column);
        if (isLEARNINGPhase)
            LOG.info("\t\t>> (constrained disambiguation in LEARNING) , position at (" + rowBlock + "/"+totalRowBlocks+"," + column + ") " + sample_tcc + " candidates=" + candidates.size());
        else
            LOG.info("\t\t>> (constrained disambiguation in UPDATE), position at (" + rowBlock + "/"+totalRowBlocks+"," + column + ") " + sample_tcc + " (candidates)-" + candidates.size());

        return disambiguate(candidates, table, rowBlock,column);
    }

    public List<Pair<Entity, Map<String, Double>>> disambiguate(List<Entity> candidates, Table table,
                                                                         List<Integer> entity_rows, int entity_column
    ) throws KBSearchException {
        //do disambiguation scoring
        //LOG.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        TCell sample_tcc = table.getContentCell(entity_rows.get(0), entity_column);
        List<Pair<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<>();
        for (Entity c : candidates) {
            //find facts of each entity
            if (c.getAttributes() == null || c.getAttributes().size() == 0) {
                List<Attribute> attributes = kbSearch.findAttributesOfEntities(c);
                c.setAttributes(attributes);
            }
            Map<String, Double> scoreMap = disambScorer.
                    computeElementScores(c, candidates,
                            entity_column,
                            entity_rows.get(0),
                            entity_rows, table);
            disambScorer.computeFinal(scoreMap, sample_tcc.getText());
            Pair<Entity, Map<String, Double>> entry = new Pair<>(c, scoreMap);
            disambiguationScores.add(entry);
        }
        return disambiguationScores;
    }

    protected void addCellAnnotation(
            Table table,
            TAnnotation tableAnnotation,
            List<Integer> rowBlock,
            int table_cell_col,
            List<Pair<Entity, Map<String, Double>>> entities_and_scoreMap) {

        Collections.sort(entities_and_scoreMap, (o1, o2) -> {
            Double o2_score = o2.getValue().get(TCellAnnotation.SCORE_FINAL);
            Double o1_score = o1.getValue().get(TCellAnnotation.SCORE_FINAL);
            return o2_score.compareTo(o1_score);
        });

        String cellText = table.getContentCell(rowBlock.get(0), table_cell_col).getText();
        for (int row : rowBlock) {
            TCellAnnotation[] annotationsForCell = new TCellAnnotation[entities_and_scoreMap.size()];
            for (int i = 0; i < entities_and_scoreMap.size(); i++) {
                Pair<Entity, Map<String, Double>> e = entities_and_scoreMap.get(i);
                double score = e.getValue().get(TCellAnnotation.SCORE_FINAL);
                annotationsForCell[i] = new TCellAnnotation(cellText,
                        e.getKey(), score, e.getValue());

            }
            tableAnnotation.setContentCellAnnotations(row, table_cell_col, annotationsForCell);
        }
    }

}

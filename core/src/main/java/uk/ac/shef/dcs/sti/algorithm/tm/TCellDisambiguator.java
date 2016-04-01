package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.rep.Attribute;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.TCell;
import uk.ac.shef.dcs.sti.rep.TCellAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;

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

    public List<Pair<Entity, Map<String, Double>>> coldstartDisambiguate(List<Entity> candidates, Table table,
                                                                         List<Integer> entity_rows, int entity_column
    ) throws KBSearchException {
        //do disambiguation scoring
        //LOG.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        TCell sample_tcc = table.getContentCell(entity_rows.get(0), entity_column);
        LOG.info("\t>> coldstart disamb, candidates=" + candidates.size());
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
            boolean isLEARNINGPhase
    ) throws KBSearchException {
        TCell sample_tcc = table.getContentCell(rowBlock.get(0), column);
        if (isLEARNINGPhase)
            LOG.info("\t\t>> constrained disambiguation (LEARNING) , position at (" + rowBlock + "," + column + ") " + sample_tcc + " candidates=" + candidates.size());
        else
            LOG.info("\t\t>> constrained disambiguation (UPDATE), position at (" + rowBlock + "," + column + ") " + sample_tcc + " (candidates)-" + candidates.size());

        return coldstartDisambiguate(candidates,table,rowBlock,column);
    }

}

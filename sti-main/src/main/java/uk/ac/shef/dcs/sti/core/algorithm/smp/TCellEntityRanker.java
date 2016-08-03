package uk.ac.shef.dcs.sti.core.algorithm.smp;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.scorer.EntityScorer;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 * NE ranker creates initial disambiguation of an NE column
 */
public class TCellEntityRanker {

    private KBSearch kbSearch;
    private EntityScorer entityScorer;
    private static final Logger LOG = Logger.getLogger(TCellEntityRanker.class.getName());

    public TCellEntityRanker(KBSearch kbSearch, EntityScorer entityScorer) {
        this.kbSearch = kbSearch;
        this.entityScorer = entityScorer;
    }

    public void rankCandidateNamedEntities(
            TAnnotation tableAnnotations, Table table,
            int row, int column
    ) throws KBSearchException {
        List<Pair<Entity, Map<String, Double>>> scores = score(table, row, column);
        TCell tcc = table.getContentCell(row, column);
        TCellAnnotation[] annotations = new TCellAnnotation[scores.size()];
        int i = 0;
        for (Pair<Entity, Map<String, Double>> oo : scores) {
            TCellAnnotation ca = new TCellAnnotation(tcc.getText(), oo.getKey(),
                    oo.getValue().get(TCellAnnotation.SCORE_FINAL), oo.getValue());
            annotations[i] = ca;
            i++;
        }
        tableAnnotations.setContentCellAnnotations(row, column, annotations);
        //return sorted;
    }

    public List<Pair<Entity, Map<String, Double>>> score(Table table,
                                                         int row, int column
    ) throws KBSearchException {
        //do disambiguation scoring
        //LOG.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        TCell cell = table.getContentCell(row, column);
        List<Entity> candidates = kbSearch.findEntityCandidates(cell.getText());
        LOG.info("\t\t>> position at (" + row + "," + column + ") " +
                cell+" has candidates="+candidates.size());
        //each candidate will have a map containing multiple elements of scores. See SMPAdaptedEntityScorer
        List<Pair<Entity, Map<String, Double>>> disambiguationScores =
                new ArrayList<>();
        for (Entity c : candidates) {
            //find facts of each entity
            if (c.getAttributes() == null || c.getAttributes().size() == 0) {
                List<Attribute> facts = kbSearch.findAttributesOfEntities(c);
                c.setAttributes(facts);
            }
            Map<String, Double> scoreMap = entityScorer.
                    computeElementScores(c, candidates,
                            column, row, Collections.singletonList(row),
                            table);
            entityScorer.computeFinal(scoreMap, cell.getText());
            Pair<Entity, Map<String, Double>> entry = new Pair<>(c,scoreMap);
            disambiguationScores.add(entry);
        }
        return disambiguationScores;
    }


}

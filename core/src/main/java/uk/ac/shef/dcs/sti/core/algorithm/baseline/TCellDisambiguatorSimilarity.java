package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 *
 */
public class TCellDisambiguatorSimilarity extends TCellDisambiguatorNameMatch {

    private static final Logger LOG = Logger.getLogger(TCellDisambiguatorSimilarity.class.getName());
    private BaselineSimilarityEntityScorer entityScorer;

    public TCellDisambiguatorSimilarity(KBSearch candidateFinder,
                                        BaselineSimilarityEntityScorer entityScorer) {
        super(candidateFinder);
        this.entityScorer = entityScorer;
    }

    protected List<Pair<Entity, Map<String, Double>>> disambiguate(List<Entity> candidates,
                                                                   Table table,
                                                                   int entity_row,
                                                                   int entity_column
    ) {
        LOG.info("\t\t>> (disambiguation, position at [" + entity_row + "," + entity_column + "]: " + table.getContentCell(entity_row, entity_column) +
                " candidates=" + candidates.size() + ")");
        List<Pair<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<>();
        if (candidates.size() > 0) {
            for (Entity ec : candidates) {
                Map<String, Double> scoreElements
                        = entityScorer.computeElementScores(ec, candidates,
                        entity_column, entity_row, Collections.singletonList(entity_row),
                        table);
                entityScorer.computeFinal(scoreElements, table.getContentCell(entity_row, entity_column).getText());
                disambiguationScores.add(new Pair<>(ec, scoreElements));
            }
        }
        return disambiguationScores;
    }
}

package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 *
 */
public class TCellDisambiguatorSimilarity extends TCellDisambiguatorNameMatch {

    private static final Logger LOG = LoggerFactory.getLogger(TCellDisambiguatorSimilarity.class.getName());
    private BaselineSimilarityEntityScorer entityScorer;

    public TCellDisambiguatorSimilarity(KBProxy candidateFinder,
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

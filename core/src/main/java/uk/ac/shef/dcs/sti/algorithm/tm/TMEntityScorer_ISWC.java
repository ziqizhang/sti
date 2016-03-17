package uk.ac.shef.dcs.sti.algorithm.tm;

import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearchResultFilter;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.sti.rep.CellAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.sti.rep.LTableContentCell;
import uk.ac.shef.dcs.sti.rep.LTableContext;
import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * scoring based on how much overlap a candidate entity has with its context
 */
public class TMEntityScorer_ISWC implements EntityScorer {
    private List<String> stopWords;
    private double[] weights_of_contexts;
    private Lemmatizer lemmatizer;

    /*
    context weights: 0-row context; 1-column context; 2-table context
     */
    public TMEntityScorer_ISWC(
            List<String> stopWords,
            double[] context_weights,
            String nlpResources) throws IOException {
        if (nlpResources != null)
            lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();

        this.stopWords = stopWords;
        weights_of_contexts = context_weights;
    }



    public Map<String, Double> score(Entity candidate,
                                     List<Entity> all_candidates,
                                     int entity_source_column,
                                     int entity_source_row,
                                     List<Integer> entity_source_rows,
                                     Table table,
                                     Set<String> assigned_column_semantic_types,
                                     Entity... reference_disambiguated_entities) {
        /*if(candidate.getName().contains("Republican"))
            System.out.println();*/
        Map<String, Double> scoreMap = new HashMap<String, Double>();
        String headerText = table.getColumnHeader(entity_source_column).getHeaderText();

        String entity_name = candidate.getLabel();
        Set<String> bag_of_words_for_entity_name = new HashSet<String>(StringUtils.toBagOfWords(entity_name, true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));


        /* BOW OF THE ENTITY*/
        List<String[]> facts = candidate.getTriples();
        List<String> bag_of_words_for_entity = new ArrayList<String>();
        for (String[] f : facts) {
            if(!TableMinerConstants.USE_NESTED_RELATION_AND_FACTS_FOR_ENTITY_FEATURE && f[3].equals("y"))
                continue;
            if (FreebaseSearchResultFilter.ignoreFactFromBOW(f[0]))
                continue;
            String value = f[1];
            if (!StringUtils.isPath(value))
                bag_of_words_for_entity.addAll(StringUtils.toBagOfWords(value, true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
            else
                bag_of_words_for_entity.add(value);
        }
        if (lemmatizer != null)
            bag_of_words_for_entity = lemmatizer.lemmatize(bag_of_words_for_entity);
        bag_of_words_for_entity.removeAll(stopWords);

        /* BOW OF THE Row context*/
        double totalScore = 0.0;
        List<String> bag_of_words_for_context = new ArrayList<String>();
        //context from the row
        for (int row : entity_source_rows) {
            for (int col = 0; col < table.getNumCols(); col++) {
                if (col == entity_source_column || table.getColumnHeader(col).getTypes().get(0).equals(
                        DataTypeClassifier.DataType.ORDERED_NUMBER
                ))
                    continue;
                LTableContentCell tcc = table.getContentCell(row, col);
                bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
            }
        }
        bag_of_words_for_context.addAll(StringUtils.toBagOfWords(   //also add the column header as the row context of this entity
                headerText, true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));

        if (lemmatizer != null)
            bag_of_words_for_context = lemmatizer.lemmatize(bag_of_words_for_context);
        bag_of_words_for_context.removeAll(stopWords);
        double contextOverlapScore = CollectionUtils.scoreCoverage_against_b(bag_of_words_for_entity, bag_of_words_for_context);
        //double contextOverlapScore = scoreOverlap(bag_of_words_for_entity, bag_of_words_for_context);
        scoreMap.put(CellAnnotation.SCORE_CTX_ROW, contextOverlapScore * weights_of_contexts[0]);
        totalScore = totalScore + contextOverlapScore * weights_of_contexts[0];

        /*BOW OF Column context*/
        bag_of_words_for_context.clear();
        for (int row = 0; row < table.getNumRows(); row++) {
            if (entity_source_rows.contains(row))
                continue;
            LTableContentCell tcc = table.getContentCell(row, entity_source_column);
            bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
        }
        if (lemmatizer != null)
            bag_of_words_for_context = lemmatizer.lemmatize(bag_of_words_for_context);
        bag_of_words_for_context.removeAll(stopWords);
        contextOverlapScore = CollectionUtils.scoreCoverage_against_b(bag_of_words_for_entity, bag_of_words_for_context);
        //contextOverlapScore = scoreOverlap(bag_of_words_for_entity, bag_of_words_for_context);
        scoreMap.put(CellAnnotation.SCORE_CTX_COLUMN, contextOverlapScore * weights_of_contexts[1]);
        totalScore = totalScore + contextOverlapScore * weights_of_contexts[1];

        /*BOW OF table table context (from paragraphs etc)*/
        bag_of_words_for_context.clear();
        for (LTableContext tc : table.getContexts()) {
            bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tc.getText(), true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
        }
        if (lemmatizer != null)
            bag_of_words_for_context = lemmatizer.lemmatize(bag_of_words_for_context);
        bag_of_words_for_context.removeAll(stopWords);
        //contextOverlapScore = scoreOverlap_againstEntity(bag_of_words_for_entity, bag_of_words_for_context);
        contextOverlapScore = CollectionUtils.scoreOverlap_dice_keepFrequency(bag_of_words_for_entity, bag_of_words_for_context);
        totalScore = totalScore + contextOverlapScore * weights_of_contexts[2];
        scoreMap.put(CellAnnotation.SCORE_CTX_OTHER, contextOverlapScore * weights_of_contexts[2]);
        double name_and_context_match_score = CollectionUtils.scoreOverlap_dice_keepFrequency(bag_of_words_for_entity_name,
                bag_of_words_for_context);

        /*REFERENCE ENTITY CONTEXT*/
        //refrence entities, if any
        Set<Entity> reference_non_duplicate = new HashSet<>();
        for (Entity ec : reference_disambiguated_entities)
            reference_non_duplicate.add(ec);
        double sum = 0.0;
        for (Entity ec : reference_non_duplicate) {
            bag_of_words_for_context.clear();

            for (String[] f : ec.getTriples()) {
                String value = f[1];
                if (!StringUtils.isPath(value))
                    bag_of_words_for_context.addAll(StringUtils.toBagOfWords(value, true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
                else
                    bag_of_words_for_context.add(value);
            }
            if (lemmatizer != null)
                bag_of_words_for_context = lemmatizer.lemmatize(bag_of_words_for_context);
            bag_of_words_for_context.removeAll(stopWords);

            double similarity_with_other_entity =
                    CollectionUtils.scoreOverlap_dice_keepFrequency(bag_of_words_for_entity, bag_of_words_for_context);
            sum = sum + similarity_with_other_entity;
        }
        double score_with_other_disambiguated_reference_entities = reference_disambiguated_entities.length > 0 ? sum / reference_disambiguated_entities.length : 0;
        totalScore = totalScore + score_with_other_disambiguated_reference_entities * weights_of_contexts[4];
        scoreMap.put(CellAnnotation.SCORE_COOCCUR_ENTITIES, score_with_other_disambiguated_reference_entities * weights_of_contexts[4]);

        /* TYPE MATCH SCORE */
        if (assigned_column_semantic_types.size() > 0 && candidate.getTypes().size() > 0) {
            bag_of_words_for_context.clear();
            bag_of_words_for_context.addAll(assigned_column_semantic_types);
            Set<String> types_strings = new HashSet<>(candidate.getTypeIds());
            /*for (String[] type : candidate.getTypes())
                types_strings.add(type[0]);*/
            double score_type_match = CollectionUtils.scoreOverlap_dice(bag_of_words_for_context, types_strings);
            scoreMap.put(CellAnnotation.SCORE_TYPE_MATCH, score_type_match);
        }

        /*NAME MATCH SCORE */
        String cell_text = table.getContentCell(entity_source_rows.get(0), entity_source_column).getText();
        Set<String> bag_of_words_for_cell_text = new HashSet<String>(StringUtils.toBagOfWords(cell_text, true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
        double name_score = CollectionUtils.scoreOverlap_dice(bag_of_words_for_cell_text, bag_of_words_for_entity_name);
        Set<String> intersection = new HashSet<String>(bag_of_words_for_cell_text);
        intersection.retainAll(bag_of_words_for_entity_name);

        scoreMap.put(CellAnnotation.SCORE_NAME_MATCH, Math.sqrt(name_score));
        scoreMap.put("matched_name_tokens",(double)intersection.size());

        /*double name_score = NameMatch_scorer.compute_order_matters(
                candidate,
                cell_text,
                other_candidate_answered_to_query);
        scoreMap.put(CellAnnotation.SCORE_NAME_MATCH, name_score);
*/
        Set<String> headerTokens = new HashSet<String>(lemmatizer.lemmatize(
                StringUtils.toBagOfWords(headerText, true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW)
        ));
        headerTokens.removeAll(stopWords);
        double name_and_header_match_score = CollectionUtils.scoreOverlap_dice(bag_of_words_for_entity_name, headerTokens);

        scoreMap.put(CellAnnotation.SCORE_NAME_MATCH_CTX, name_and_header_match_score/* +
                name_and_col_match_score + name_and_context_match_score*/);


        return scoreMap;
    }

    @Deprecated
    /*public static double computeFinal(Map<String, Double> scoreMap, String cellTextOriginal) {
        double sum = 0.0;
        for (Map.Entry<String, Double> e : scoreMap.entrySet()) {
            if (e.getKey().startsWith("ctx_"))
                sum += e.getValue();
            if(e.getKey().equals(CellAnnotation.SCORE_NAME_MATCH))
                sum+=e.getValue();
        }

        scoreMap.put(CellAnnotation.SCORE_FINAL, sum);
        return sum;
    }*/



    public double computeFinal(Map<String, Double> scoreMap, String cellTextOriginal) {
        double sum = 0.0;
        for (Map.Entry<String, Double> e : scoreMap.entrySet()) {
            if (e.getKey().startsWith("ctx_"))
                sum += e.getValue();
        }
        Double nameMatch = scoreMap.get(CellAnnotation.SCORE_NAME_MATCH);
        if (nameMatch != null)
            sum = sum *nameMatch;
        scoreMap.put(CellAnnotation.SCORE_FINAL, sum);
        return sum;
    }



}

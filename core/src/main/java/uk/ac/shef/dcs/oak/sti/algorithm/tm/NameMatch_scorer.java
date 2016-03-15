package uk.ac.shef.dcs.oak.sti.algorithm.tm;

import uk.ac.shef.dcs.kbsearch.rep.Entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class NameMatch_scorer {

    private static List<String> stopWords = new ArrayList<String>();
    static{
        stopWords.add("The");
        stopWords.add("the");
        stopWords.add("of");
        stopWords.add("and");
        stopWords.add("or");
    }

    public static double compute_order_matters(Entity entity,
                                               String tableCellText,
                                               List<Entity> entity_list
    ){
        List<String> entity_name_tokens = toTokens(entity.getLabel());
        List<String> table_cell_tokens = toTokens(tableCellText);

        double similarity = calculate_similarity_wordOrder(entity_name_tokens, table_cell_tokens);
        /*double disambiguation_weight = calculate_disambiguation_weight(
                entity,
                entity_list);*/

        return similarity/**disambiguation_weight*/;
    }

    private static double calculate_disambiguation_weight(Entity entity, List<Entity> entity_list) {
        int count=0;
        for(Entity ec: entity_list){
            if(entity.getLabel().equals(ec.getLabel()))
                count++;
        }
        return 1.0/count;
    }

    private static double calculate_similarity_wordOrder(List<String> entity_name_tokens, List<String> table_cell_tokens) {
        Set<String> intersection = new HashSet<String>(entity_name_tokens);
        intersection.retainAll(table_cell_tokens);
        if(intersection.size()==0)
            return 0.0;

        double sum=0.0;
        for(String tok: intersection){
            int index = table_cell_tokens.indexOf(tok);
            int weight = table_cell_tokens.size()-index;
            double score = (double) weight/table_cell_tokens.size();
            sum+=score;
        }

        return sum/intersection.size();
    }

    private static List<String> toTokens(String label){
        List<String> tokens = new ArrayList<String>();
        for(String t : label.split("\\s+")){
            t=t.trim();
            if(t.length()>0 &&!stopWords.contains(t)){
                tokens.add(t);
            }
        }
        return tokens;
    }
}

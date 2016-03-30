package uk.ac.shef.dcs.sti.algorithm.tm;

import uk.ac.shef.dcs.sti.rep.TCellAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.sti.rep.TAnnotation;

import java.util.*;

/**
 * enforcing one name per sense per discourse
 */
public class ONPSPD_Enforcer {

    public static void enforce(Table table,
                               TAnnotation table_annotation, int col) {
        Map<String, Set<String>> entityId_to_cellTexts = new HashMap<String, Set<String>>();
        Map<Integer, Set<Integer>> elements_to_update = new HashMap<Integer, Set<Integer>>();

        for (int r = 0; r < table.getNumRows(); r++) {
            TCellAnnotation[] annotations = table_annotation.getContentCellAnnotations(r, col);
            if (annotations != null && annotations.length > 0) {
                for (int i = 0; i < annotations.length; i++) {
                    TCellAnnotation ca = annotations[i];
                    String entityId = ca.getAnnotation().getId();
                    Set<String> cellTexts = entityId_to_cellTexts.get(entityId);

                    if (cellTexts == null) {
                        cellTexts = new HashSet<String>();
                    }
                    cellTexts.add(ca.getTerm());
                    if (cellTexts.size() > 1) {
                        Set<Integer> cell_annotation_indexes = elements_to_update.get(r);
                        cell_annotation_indexes = cell_annotation_indexes == null ? new HashSet<Integer>() : cell_annotation_indexes;
                        cell_annotation_indexes.add(i);
                        elements_to_update.put(r, cell_annotation_indexes);
                    }
                    entityId_to_cellTexts.put(entityId, cellTexts);
                }

            }
        }

        for (int r = 0; r < table.getNumRows(); r++) {
            TCellAnnotation[] annotations = table_annotation.getContentCellAnnotations(r, col);
            List<TCellAnnotation> revised = new ArrayList<TCellAnnotation>();
            if (annotations != null && annotations.length > 0) {
                for (int i = 0; i < annotations.length; i++) {
                    TCellAnnotation ca = annotations[i];
                    String entityId = ca.getAnnotation().getId();
                    Set<String> cellTexts = entityId_to_cellTexts.get(entityId);

                    if (cellTexts != null || cellTexts.size() > 2) {
                        double score = ca.getFinalScore();
                        score = score / Math.sqrt(cellTexts.size());
                        ca.setFinalScore(score);
                    }

                    revised.add(ca);
                }
                if (revised.size() != 0) {
                    Collections.sort(revised);
                    table_annotation.setContentCellAnnotations(r, col, revised.toArray(new TCellAnnotation[0]));
                }
            }
        }

    }
}

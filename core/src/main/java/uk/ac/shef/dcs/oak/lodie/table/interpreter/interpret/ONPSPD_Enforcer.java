package uk.ac.shef.dcs.oak.lodie.table.interpreter.interpret;

import uk.ac.shef.dcs.oak.lodie.table.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;

import java.util.*;

/**
 * enforcing one name per sense per discourse
 */
public class ONPSPD_Enforcer {

    public static void enforce(LTable table,
                               LTableAnnotation table_annotation, int col) {
        Map<String, Set<String>> entityId_to_cellTexts = new HashMap<String, Set<String>>();
        Map<Integer, Set<Integer>> elements_to_update = new HashMap<Integer, Set<Integer>>();

        for (int r = 0; r < table.getNumRows(); r++) {
            CellAnnotation[] annotations = table_annotation.getContentCellAnnotations(r, col);
            if (annotations != null && annotations.length > 0) {
                for (int i = 0; i < annotations.length; i++) {
                    CellAnnotation ca = annotations[i];
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
            CellAnnotation[] annotations = table_annotation.getContentCellAnnotations(r, col);
            List<CellAnnotation> revised = new ArrayList<CellAnnotation>();
            if (annotations != null && annotations.length > 0) {
                for (int i = 0; i < annotations.length; i++) {
                    CellAnnotation ca = annotations[i];
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
                    table_annotation.setContentCellAnnotations(r, col, revised.toArray(new CellAnnotation[0]));
                }
            }
        }

    }
}

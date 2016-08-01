package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;
import java.util.List;

/**
 * this simply chooses column type based on relations' expected types
 */
public class LiteralColumnTaggerImpl implements LiteralColumnTagger {
    private static final Logger LOG = Logger.getLogger(LiteralColumnTaggerImpl.class.getName());
    private int[] ignoreColumns;

    public LiteralColumnTaggerImpl(
            int... ignoreColumns) {
        this.ignoreColumns = ignoreColumns;

    }
    
    public void setIgnoreColumns(int... ignoreCols) {
      this.ignoreColumns = ignoreCols;
    }

    public void annotate(Table table, TAnnotation annotations, Integer... neColumns) throws KBSearchException {
        //for each column that has a relation with the subject column, infer its type
        Map<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>>
                relationAnnotations = annotations.getCellcellRelations();

        //LOG.info("\t>> Annotating literal columns");
        for (Map.Entry<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>>
                e : relationAnnotations.entrySet()) {
            RelationColumns subcol_objcol = e.getKey();
            if (ignoreColumn(subcol_objcol.getObjectCol())) continue;

            LOG.info("\t\t>> object column= " + subcol_objcol.getObjectCol());
            boolean skip = false;

            //check if the object column is an ne column, and whether that ne column is already annotated
            //if so we do not need to annotate this column, we just need to skip it
            for (int i : neColumns) {
                boolean isColumn_acronym_or_code = table.getColumnHeader(i).getFeature().isAcronymColumn();
                if (i == subcol_objcol.getObjectCol() && !isColumn_acronym_or_code) {
                    if (annotations.getHeaderAnnotation(i) != null &&
                            annotations.getHeaderAnnotation(i).length > 0) {
                        skip = true;
                        break;
                    }
                }
            }
            if (skip) {
                LOG.debug("\t\t>> skipped object column (possibly NE column) " + subcol_objcol.getObjectCol());
                continue;
            }

            List<TColumnHeaderAnnotation> candidates = new ArrayList<>();
            List<TColumnColumnRelationAnnotation> relations =
                    annotations.getColumncolumnRelations().
                            get(subcol_objcol); //get the relation annotations between subject col and this column
            for (TColumnColumnRelationAnnotation relation : relations) {
                //we simply create a new clazz using the relation's uri and label
                TColumnHeaderAnnotation hAnn = new TColumnHeaderAnnotation(table.getColumnHeader(subcol_objcol.getObjectCol()).getHeaderText(),
                        new Clazz(relation.getRelationURI(), relation.getRelationLabel()),
                        relation.getFinalScore());
                if(!candidates.contains(hAnn))
                    candidates.add(hAnn);
            }

            List<TColumnHeaderAnnotation> sorted = new ArrayList<>(candidates);
            Collections.sort(sorted);
            annotations.setHeaderAnnotation(subcol_objcol.getObjectCol(), sorted.toArray(new TColumnHeaderAnnotation[0]));
        }

    }

    private boolean ignoreColumn(Integer i) {
        if (i != null) {
            for (int a : ignoreColumns) {
                if (a == i)
                    return true;
            }
        }
        return false;
    }

}

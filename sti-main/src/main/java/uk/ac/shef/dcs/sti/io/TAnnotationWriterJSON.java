package uk.ac.shef.dcs.sti.io;

import com.google.gson.Gson;
import javafx.util.Pair;

import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.sti.core.model.*;
import uk.ac.shef.dcs.sti.util.TripleGenerator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

/**
 * Created by - on 23/06/2016.
 */
public class TAnnotationWriterJSON extends TAnnotationWriter {
    protected Gson gson = new Gson();

    public TAnnotationWriterJSON(TripleGenerator tripleGenerator) {
        super(tripleGenerator);
    }

    protected void writeCellKeyFile(Table table, TAnnotation table_annotation, String cell_key) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(cell_key);
        PrintWriter p_cellCllass= new PrintWriter(cell_key+".clazz");

        List<JSONOutputCellAnnotation> jsonCells = new ArrayList<>();
        List<Pair<String, Set<String>>> cellCandidateEntityAndClass = new ArrayList<>();
        for (int r = 0; r < table.getNumRows(); r++) {
            for (int c = 0; c < table.getNumCols(); c++) {
                JSONOutputCellAnnotation jc = new JSONOutputCellAnnotation(r, c, table.getContentCell(r,c).getText());
                TCellAnnotation[] cans = table_annotation.getContentCellAnnotations(r, c);
                //if (cans != null && cans.length > 0) {
                    for (TCellAnnotation ca : cans) {
                        jc.add(ca);

                        Set<String> classes = new HashSet<>();
                        for(Clazz clazz: ca.getAnnotation().getTypes())
                            classes.add(clazz.getId());
                        Pair<String, Set<String>> entityClasses = new Pair<>(ca.getAnnotation().getId(),
                                classes);
                        cellCandidateEntityAndClass.add(entityClasses);
                    }
                //}
                jsonCells.add(jc);
            }
        }

        String string = gson.toJson(jsonCells);
        p.println(string);
        p.close();

        string=gson.toJson(cellCandidateEntityAndClass);
        p_cellCllass.println(string);
        p_cellCllass.close();
    }

    protected void writeRelationKeyFile(TAnnotation table_annotation, String relation_key) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(relation_key);
        PrintWriter pc = new PrintWriter(relation_key+".cell");

        List<JSONOutputRelationAnnotation> jrs = new ArrayList<>();
        for (Map.Entry<RelationColumns, java.util.List<TColumnColumnRelationAnnotation>> e :
                table_annotation.getColumncolumnRelations().entrySet()) {
            int subCol = e.getKey().getSubjectCol();
            int objCol = e.getKey().getObjectCol();
            JSONOutputRelationAnnotation jr = new JSONOutputRelationAnnotation(subCol, objCol);
            java.util.List<TColumnColumnRelationAnnotation> relations = e.getValue();
            Collections.sort(relations);
            for (TColumnColumnRelationAnnotation hr : relations) {
                jr.add(hr);
            }
            jrs.add(jr);
        }
        String string = gson.toJson(jrs);
        p.println(string);
        p.close();


        List<JSONOutputRelationAnnotationPerRow> jrcs = new ArrayList<>();
        for (Map.Entry<RelationColumns, java.util.List<TColumnColumnRelationAnnotation>> e :
                table_annotation.getColumncolumnRelations().entrySet()) {
            int subCol = e.getKey().getSubjectCol();
            int objCol = e.getKey().getObjectCol();

            Map<Integer, java.util.List<TCellCellRelationAnotation>> rpr=
                    table_annotation.getRelationAnnotationsBetween(subCol, objCol);

            for(Map.Entry<Integer, java.util.List<TCellCellRelationAnotation>> en:
                    rpr.entrySet()){
                int row=en.getKey();
                List<TCellCellRelationAnotation> rprc = en.getValue();
                JSONOutputRelationAnnotationPerRow o = new JSONOutputRelationAnnotationPerRow(subCol,objCol,row);
                for(TCellCellRelationAnotation cc: rprc){
                    o.add(cc);
                }
                jrcs.add(o);
            }

        }
        string = gson.toJson(jrcs);
        pc.println(string);
        pc.close();


    }

    protected void writeHeaderKeyFile(Table table, TAnnotation table_annotation, String header_key) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(header_key);

        List<JSONOutputColumnAnnotation> jsonColumns = new ArrayList<>();
        for (int c = 0; c < table.getNumCols(); c++) {
            TColumnHeaderAnnotation[] anns = table_annotation.getHeaderAnnotation(c);
            JSONOutputColumnAnnotation jc = new JSONOutputColumnAnnotation(c, table.getColumnHeader(c).getHeaderText());
            //if (anns != null && anns.length > 0) {
                for (TColumnHeaderAnnotation ha : anns) {
                    jc.add(ha);
                }
            //}
            jsonColumns.add(jc);
        }
        String string = gson.toJson(jsonColumns);
        p.println(string);
        p.close();

    }
}

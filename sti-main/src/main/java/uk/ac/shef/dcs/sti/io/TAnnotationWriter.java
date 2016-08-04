package uk.ac.shef.dcs.sti.io;

import uk.ac.shef.dcs.sti.util.TripleGenerator;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**

 */
public class TAnnotationWriter {

    protected String linkPrefix = "http://www.freebase.com";
    protected boolean showLosingCandidates = true;
    protected TripleGenerator tripleGenerator;

    public TAnnotationWriter(TripleGenerator tripleGenerator) {
        this.tripleGenerator = tripleGenerator;
    }


    public void writeHTML(Table table, TAnnotation tab_annotations, String outFile) throws FileNotFoundException {
        StringBuilder table_sb = new StringBuilder();
        table_sb.append("<html><body>\n");
        String sourceId = table.getSourceId();
        sourceId = sourceId.replaceAll("\\\\", "/");
        int trimStart = sourceId.lastIndexOf("/");
        int trimEnd = sourceId.lastIndexOf(".htm");
        trimStart = trimStart == -1 ? 0 : trimStart;
        trimEnd = trimEnd == -1 ? sourceId.length() : trimEnd;
        sourceId = sourceId.substring(trimStart + 1, trimEnd);

        List<TableTriple> triples = tripleGenerator.generate_newTriples(tab_annotations, table);
        table_sb.append("source:" + sourceId + " with " + triples.size() + "<a href=\"" + outFile + ".attributes.html\"> new attributes</a>.");
        writeTriples(triples, outFile + ".attributes.html");

        table_sb.append("<h1>Table column types and entity disambiguation</h1>\n");
        table_sb.append("<table border=\"1\">");
        table_sb.append(writeHeader(table, tab_annotations));
        table_sb.append(writeCell(table, tab_annotations));
        table_sb.append("</table>");

        table_sb.append("<h1>Table subject column and binary relations</h1>");
        table_sb.append("<table border=\"1\">");
        table_sb.append(writeRelation_inHeader(table, tab_annotations));
        table_sb.append(writeRelation_inCell(table, tab_annotations));
        table_sb.append("</table>\n");

        table_sb.append("</body></html>");

        PrintWriter p = new PrintWriter(outFile);
        p.println(table_sb.toString());
        p.close();

        File f = new File(outFile);
        String header_key = f.getPath() + ".header.keys";
        String relation_key = f.getPath() + ".relation.keys";
        String cell_key = f.getPath() + ".cell.keys";
        writeHeaderKeyFile(table, tab_annotations, header_key);
        writeRelationKeyFile(tab_annotations, relation_key);
        writeCellKeyFile(table, tab_annotations, cell_key);

    }

    protected void writeCellKeyFile(Table table, TAnnotation table_annotation, String cell_key) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(cell_key);
        for (int r = 0; r < table.getNumRows(); r++) {
            for (int c = 0; c < table.getNumCols(); c++) {
                TCellAnnotation[] cans = table_annotation.getContentCellAnnotations(r, c);
                if (cans != null && cans.length > 0) {
                    StringBuilder s = new StringBuilder();
                    s.append(r).append(",").append(c).append("=");
                    double prevScore=0.0;
                    for (TCellAnnotation ca : cans) {
                        if (prevScore == 0.0) {

                            s.append(ca.getAnnotation().getId());
                            prevScore=ca.getFinalScore();
                        }
                        else{
                            if(ca.getFinalScore()==prevScore){
                                s.append("=").append(ca.getAnnotation().getId());
                            }
                            else
                                s.append("|").append(ca.getAnnotation().getId());
                        }
                    }
                    p.println(s.toString());
                }
            }
        }
        p.close();
    }

    protected void writeRelationKeyFile(TAnnotation table_annotation, String relation_key) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(relation_key);
        for (Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>> e :
                table_annotation.getColumncolumnRelations().entrySet()) {
            int subCol = e.getKey().getSubjectCol();
            int objCol = e.getKey().getObjectCol();
            List<TColumnColumnRelationAnnotation> relations = e.getValue();
            Collections.sort(relations);
            StringBuilder s = new StringBuilder();
            double prevScore=0.0;
            for (TColumnColumnRelationAnnotation hr : relations) {
                if (prevScore == 0.0) {

                    s.append(hr.getRelationURI());
                    prevScore=hr.getFinalScore();
                }
                else{
                    if(hr.getFinalScore()==prevScore){
                        s.append("=").append(hr.getRelationURI());
                    }
                    else
                        s.append("|").append(hr.getRelationURI());
                }
            }
            p.println(subCol + "," + objCol + "=" + s.toString());
        }
        p.close();
    }

    protected void writeHeaderKeyFile(Table table, TAnnotation table_annotation, String header_key) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(header_key);

        for (int c = 0; c < table.getNumCols(); c++) {
            TColumnHeaderAnnotation[] anns = table_annotation.getHeaderAnnotation(c);
            if (anns != null && anns.length > 0) {
                StringBuilder s = new StringBuilder();
                s.append(c).append("=");

                double prevScore = 0.0;
                for (TColumnHeaderAnnotation ha : anns) {
                    if (prevScore == 0.0) {

                        s.append(ha.getAnnotation().getId());
                        prevScore=ha.getFinalScore();
                    }
                    else{
                        if(ha.getFinalScore()==prevScore){
                            s.append("=").append(ha.getAnnotation().getId());
                        }
                        else
                            s.append("|").append(ha.getAnnotation().getId());
                    }
                }
                if(table.getColumnHeader(c).getFeature().getMostFrequentDataType().getType().equals(
                        DataTypeClassifier.DataType.NAMED_ENTITY
                ))
                    s.append("\t\t\t___NE");
                p.println(s.toString());
            }
        }

        p.close();
    }

    protected void writeTriples(List<TableTriple> triples, String outFile) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(outFile);
        p.println("<html><body>");
        for (TableTriple ltt : triples) {
            p.println("<br>&lt;" + ltt.getSubject_annotation() + "," + ltt.getRelation_annotation() + "," + ltt.getObject_annotation() + "&gt;, " +
                    "(" + ltt.getSubject() + "," + ltt.getObject() + "), " + "[" + ltt.getSubject_position()[0] + "," + ltt.getSubject_position()[1] + "][" +
                    ltt.getObject_position()[0] + "," + ltt.getObject_position()[1] + "]</br>");
        }
        p.println("</body></html>");
        p.close();
    }

    protected String writeRelation_inCell(Table table, TAnnotation tab_annotations) {
        StringBuilder out = new StringBuilder();
        out.append("<tr>\n");
        for (int row = 0; row < table.getNumRows(); row++) {
            for (int col = 0; col < table.getNumCols(); col++) {
                String color = col == tab_annotations.getSubjectColumn() ? " bgcolor=\"yellow\"" : "";
                TCell cell = table.getContentCell(row, col);

                out.append("\t<td").append(color).append(">").append(cell.getText()).append(cell.getOtherText()).append("</td>\n");

                //then annotations
                if (col == tab_annotations.getSubjectColumn()) {
                    out.append("\t<td");
                    StringBuilder annotation = new StringBuilder();
                    TCellAnnotation[] cAnns = tab_annotations.getContentCellAnnotations(row, col);
                    if (cAnns == null)
                        annotation.append(">-");
                    else {
                        annotation.append(" bgcolor=\"#00FF00\">");
                        for (int i = 0; i < cAnns.length; i++) {
                            TCellAnnotation cAnn = cAnns[i];
                            if (i == 0) { //the winning annotation
                                annotation.append("<br><b>").append(generateCellAnnotationString(cAnn)).append("</b></br>");
                            } else if (showLosingCandidates) {  //others
                                annotation.append("<br><font color=\"grey\" size=\"1\">").
                                        append(generateCellAnnotationString(cAnn)).append("</font></br>");
                            }
                        }
                    }
                    annotation.append("\t</td>\n");
                    out.append(annotation);
                } else {
                    out.append("\t<td");

                    StringBuilder annotation = new StringBuilder();
                    RelationColumns key = new RelationColumns(tab_annotations.getSubjectColumn(), col);
                    Map<Integer, List<TCellCellRelationAnotation>> tmp = tab_annotations.getRelationAnnotationsBetween(key.getSubjectCol(), key.getObjectCol());
                    if (tmp == null) {
                        annotation.append(">-");
                        annotation.append("</td>\n");
                        out.append(annotation);
                        continue;
                    }
                    List<TCellCellRelationAnotation> crAnns = tmp.get(row);

                    if (crAnns == null)
                        annotation.append(">-");
                    else {
                        Collections.sort(crAnns);
                        annotation.append(" bgcolor=\"#00FF00\">");
                        for (int i = 0; i < crAnns.size(); i++) {
                            TCellCellRelationAnotation crAnn = crAnns.get(i);
                            if (i == 0) { //the winning annotation
                                annotation.append("<br><b>").append(generateAcrossCellRelationString(crAnn)).append("</b></br>");
                            } else if (showLosingCandidates) {  //others
                                annotation.append("<br><font color=\"grey\" size=\"1\">").
                                        append(generateAcrossCellRelationString(crAnn)).append("</font></br>");
                            }
                        }
                    }

                    annotation.append("\t</td>\n");
                    out.append(annotation);
                }
            }
            out.append("</tr>\n");
        }
        return out.toString();
    }

    protected String writeRelation_inHeader(Table table, TAnnotation tab_annotations) {
        StringBuilder out = new StringBuilder();
        out.append("<tr>\n");
        for (int col = 0; col < table.getNumCols(); col++) {
            String color = col == tab_annotations.getSubjectColumn() ? " bgcolor=\"yellow\"" : "";
            TColumnHeader header = table.getColumnHeader(col);
            if(header==null)
                continue;
            out.append("\t<th").append(color).append(">").append(header.getHeaderText()).append("</th>\n");

            //then annotations
            if (col == tab_annotations.getSubjectColumn()) {
                out.append("\t<th");
                StringBuilder annotation = new StringBuilder();
                TColumnHeaderAnnotation[] hAnns = tab_annotations.getHeaderAnnotation(col);
                if (hAnns == null)
                    annotation.append(">-");
                else {
                    annotation.append(" bgcolor=\"#00FF00\">");
                    double best_score = 0.0;
                    for (int i = 0; i < hAnns.length; i++) {
                        TColumnHeaderAnnotation hAnn = hAnns[i];
                        if (i == 0) { //the winning annotation
                            annotation.append("<br><b>").append(generateHeaderAnnotationString(hAnn)).append("</b></br>");
                            best_score = hAnn.getFinalScore();
                        } else if (hAnn.getFinalScore() == best_score) {
                            annotation.append("<br><b>").append(generateHeaderAnnotationString(hAnn)).append("</b></br>");
                        } else if (showLosingCandidates) {  //others
                            annotation.append("<br><font color=\"grey\" size=\"1\">").
                                    append(generateHeaderAnnotationString(hAnn)).append("</font></br>");
                        }
                    }
                }
                annotation.append("\t</th>\n");
                out.append(annotation);
            } else {
                out.append("\t<th");

                StringBuilder annotation = new StringBuilder();
                RelationColumns key = new RelationColumns(tab_annotations.getSubjectColumn(), col);
                List<TColumnColumnRelationAnnotation> hAnns = tab_annotations.getColumncolumnRelations().get(key);

                if (hAnns == null)
                    annotation.append(">-");
                else {
                    Collections.sort(hAnns);
                    annotation.append(" bgcolor=\"#00FF00\">");
                    for (int i = 0; i < hAnns.size(); i++) {
                        TColumnColumnRelationAnnotation hAnn = hAnns.get(i);
                        if (i == 0) { //the winning annotation
                            annotation.append("<br><b>").append(generateAcrossHeaderRelationString(hAnn)).append("</b></br>");
                        } else if (showLosingCandidates) {  //others
                            annotation.append("<br><font color=\"grey\" size=\"1\">").
                                    append(generateAcrossHeaderRelationString(hAnn)).append("</font></br>");
                        }
                    }
                }

                annotation.append("\t</th>\n");
                out.append(annotation);
            }
        }
        out.append("</tr>\n");
        return out.toString();
    }


    protected String writeHeader(Table table, TAnnotation tab_annotations) {
        StringBuilder out = new StringBuilder();
        out.append("<tr>\n");
        for (int col = 0; col < table.getNumCols(); col++) {
            TColumnHeader header = table.getColumnHeader(col);
            if(header==null)
                continue;
            out.append("\t<th>").append(header.getHeaderText()).append("</th>\n");

            //then annotations
            out.append("\t<th");
            StringBuilder annotation = new StringBuilder();
            TColumnHeaderAnnotation[] hAnns = tab_annotations.getHeaderAnnotation(col);
            if (hAnns == null)
                annotation.append(">-");
            else {
                annotation.append(" bgcolor=\"#00FF00\">");
                double best_score = 0.0;
                for (int i = 0; i < hAnns.length; i++) {
                    TColumnHeaderAnnotation hAnn = hAnns[i];
                    if (i == 0) { //the winning annotation
                        annotation.append("<br><b>").append(generateHeaderAnnotationString(hAnn)).append("</b></br>");
                        best_score = hAnn.getFinalScore();
                    } else if (hAnn.getFinalScore() == best_score) {
                        annotation.append("<br><b>").append(generateHeaderAnnotationString(hAnn)).append("</b></br>");
                    } else if (showLosingCandidates) {  //others
                        annotation.append("<br><font color=\"grey\" size=\"1\">").
                                append(generateHeaderAnnotationString(hAnn)).append("</font></br>");
                    }
                }
            }
            annotation.append("\t</th>\n");
            out.append(annotation);
        }
        out.append("</tr>\n");
        return out.toString();
    }

    protected String writeCell(Table table, TAnnotation tab_annotations) {
        StringBuilder out = new StringBuilder();

        for (int row = 0; row < table.getNumRows(); row++) {
            out.append("<tr>\n");
            for (int col = 0; col < table.getNumCols(); col++) {
                TCell tcc = table.getContentCell(row, col);
                out.append("\t<td>").append(tcc.getText()).append(tcc.getOtherText()).append("<font color=\"grey\">").append(" [").
                        append(tcc.getType().getValue()).append("]</font>").
                        append("</td>\n");

                //then annotations
                out.append("\t<td");
                StringBuilder annotation = new StringBuilder();
                TCellAnnotation[] cAnns = tab_annotations.getContentCellAnnotations(row, col);
                if (cAnns == null)
                    annotation.append(">-");
                else {
                    annotation.append(" bgcolor=\"#00FF00\">");
                    for (int i = 0; i < cAnns.length; i++) {
                        TCellAnnotation cAnn = cAnns[i];
                        if (i == 0) { //the winning annotation
                            annotation.append("<br><b>").append(generateCellAnnotationString(cAnn)).append("</b></br>");
                        } else if (showLosingCandidates) {  //others
                            annotation.append("<br><font color=\"grey\" size=\"1\">").
                                    append(generateCellAnnotationString(cAnn)).append("</font></br>");
                        }
                    }
                }
                annotation.append("\t</td>\n");
                out.append(annotation);
            }
            out.append("</tr>\n");
        }
        return out.toString();
    }

    protected Object generateCellAnnotationString(TCellAnnotation cAnn) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"" + linkPrefix + cAnn.getAnnotation().getId() + "\">").
                append(cAnn.getAnnotation().getLabel()).append("</a>").
                append("=").append(Math.round(cAnn.getFinalScore() * 100.0) / 100.0).append(cAnn.getAnnotation().getTypeIds());
        return sb.toString();
    }

    protected String generateHeaderAnnotationString(TColumnHeaderAnnotation ha) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"" + linkPrefix + ha.getAnnotation().getId() + "\">").
                append(ha.getAnnotation().getId()).append("(").append(ha.getAnnotation().getId()).append(")</a>").
                append("=").append(Math.round(ha.getFinalScore() * 100.0) / 100.0).append(ha.getSupportingRows());
        return sb.toString();
    }

    protected String generateAcrossHeaderRelationString(TColumnColumnRelationAnnotation ha) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"" + linkPrefix + ha.getRelationURI() + "\">").
                append(ha.getRelationURI()).
                append("</a>").
                append("=").append(Math.round(ha.getFinalScore() * 100.0) / 100.0).append(ha.getSupportingRows());
        return sb.toString();
    }

    protected String generateAcrossCellRelationString(TCellCellRelationAnotation ca) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"" + linkPrefix + ca.getRelationURI() + "\">").
                append(ca.getRelationURI()).
                append("</a>").
                append("=").append(Math.round(ca.getWinningAttributeMatchScore() * 100.0) / 100.0);
        return sb.toString();
    }
}

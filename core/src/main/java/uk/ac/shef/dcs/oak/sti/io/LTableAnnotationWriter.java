package uk.ac.shef.dcs.oak.sti.io;

import java.io.BufferedWriter;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.TripleGenerator;
import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.rep.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import uk.ac.shef.dcs.oak.util.ObjObj;

/**

 */
public class LTableAnnotationWriter {

    protected String linkPrefix = "http://www.freebase.com";

    protected boolean showLosingCandidates = true;

    protected TripleGenerator tripleGenerator;

    public LTableAnnotationWriter(TripleGenerator tripleGenerator) {
        this.tripleGenerator = tripleGenerator;
    }

    public void writeHTML(LTable table, LTableAnnotation tab_annotations, String outFile) throws FileNotFoundException {
        StringBuilder table_sb = new StringBuilder();
        table_sb.append("<html><body>\n");
        String sourceId = table.getSourceId();
        sourceId = sourceId.replaceAll("\\\\", "/");
        int trimStart = sourceId.lastIndexOf("/");
        int trimEnd = sourceId.lastIndexOf(".htm");
        trimStart = trimStart == -1 ? 0 : trimStart;
        trimEnd = trimEnd == -1 ? sourceId.length() : trimEnd;
        sourceId = sourceId.substring(trimStart + 1, trimEnd);

        List<LTableTriple> triples = tripleGenerator.generate_newTriples(tab_annotations, table);
        table_sb.append("source:" + sourceId + " with " + triples.size() + "<a href=\"" + outFile + ".triples.html\"> new triples</a>.");
        writeTriples(triples, outFile + ".triples.html");

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

    protected void writeCellKeyFile(LTable table, LTableAnnotation table_annotation, String cell_key) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(cell_key);
        for (int r = 0; r < table.getNumRows(); r++) {
            for (int c = 0; c < table.getNumCols(); c++) {
                CellAnnotation[] cans = table_annotation.getContentCellAnnotations(r, c);
                if (cans != null && cans.length > 0) {
                    StringBuilder s = new StringBuilder();
                    s.append(r).append(",").append(c).append("=");
                    double prevScore = 0.0;
                    for (CellAnnotation ca : cans) {
                        if (prevScore == 0.0) {

                            s.append(ca.getAnnotation().getId());
                            prevScore = ca.getFinalScore();
                        }
                        else {
                            if (ca.getFinalScore() == prevScore) {
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

    /**
     * It writes for each row
     * 
     * @param table
     * @param table_annotation
     * @param cell_key
     * @throws FileNotFoundException
     */
    public void writeBasicStatistics(File f, LTable table, LTableAnnotation table_annotation, String cell_key) throws FileNotFoundException {

        PrintWriter p = new PrintWriter(cell_key);

        //p.println("Rows;Cols;NECols;SubjectColID");

        p.print(" File: " + f.getName());
        int nRows = table_annotation.getRows();
        p.print(" Rows: " + nRows);
        int nColumns = table_annotation.getCols();
        p.print(" Cols: " + nColumns);

        p.print(" Columns: ");
        for (int i = 0; i < nColumns; i++) {
            String columnHeaderText = table.getColumnHeader(i).getHeaderText();
            p.print(columnHeaderText + ",");
        }

        int nneColumns = table_annotation.getCandidateNEForSubjectColumn().size();
        p.print(" NECols: " + nneColumns);

        //subject column and candidates

        int subjectColumnID = table_annotation.getSubjectColumn();
        String columnName = table.getColumnHeader(subjectColumnID).getHeaderText();;
        p.print(" SubjCol: " + columnName);
        p.print(" SubjColCandidates:: ");
        for (ObjObj object : table_annotation.getCandidateNEForSubjectColumn()) {
            int columnID = (int) object.getMainObject();
            columnName = table.getColumnHeader(columnID).getHeaderText();;
            p.print(columnName);
            ObjObj otherObject = (ObjObj) object.getOtherObject();
            p.print(":" + otherObject.getMainObject());
            p.print(",");

        }

        //classification 
        p.print(" Classification:: ");
        for (ObjObj object : table_annotation.getCandidateNEForSubjectColumn()) {
            int columnID = (int) object.getMainObject();
            columnName = table.getColumnHeader(columnID).getHeaderText();;
            p.print(columnName + ":");

            //get classification
            HeaderAnnotation[] anns = table_annotation.getHeaderAnnotation(columnID);
            if (anns != null && anns.length > 0) {
                StringBuilder s = new StringBuilder();
                //s.append("\n" + columnName);

                //get only top concepts (with highest score - more concept can have the highest score!)
                double prevScore = 0.0;
                for (HeaderAnnotation ha : anns) {

                    if (prevScore == 0.0) {
                        prevScore = ha.getFinalScore();
                    }

                    if (ha.getFinalScore() < prevScore) {
                        //it is not the highest score, we break (the values are sorted)
                        break;
                    }

                    s.append(ha.getAnnotation_url());
                    s.append(":" + ha.getFinalScore());
                    s.append(",");
                }
                p.print(s.toString());
            }

        }

        p.close();
    }

    /**
     * It writes for each row
     * 
     * @param table
     * @param table_annotation
     * @param cell_key
     * @throws FileNotFoundException
     */
    public void writeBasicSubjectColumnStatisticsCSV(File f, LTable table, LTableAnnotation table_annotation, String cell_key) throws FileNotFoundException {

        boolean prepareHeader = false;
        File temp = new File(cell_key);
        if (!temp.exists()) {
            // do something
            prepareHeader = true;
        }

        Charset encoding = table.getEncoding();

        PrintWriter p = null;
        try {
            p = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cell_key, true), encoding)));

            //PrintWriter p = new PrintWriter(cell_key);

            if (prepareHeader) {
                p.println("File;Rows;Cols;Columns;SubjCol;NECols;SubjColCandidates");
            }

            p.append(f.getName());
            p.append(";");
            int nRows = table_annotation.getRows();
            p.append(String.valueOf(nRows));
            p.append(";");
            int nColumns = table_annotation.getCols();
            p.append(String.valueOf(nColumns));
            p.append(";");

            //column headers
            for (int i = 0; i < nColumns; i++) {
                String columnHeaderText = table.getColumnHeader(i).getHeaderText();
                p.append(columnHeaderText.trim());
                if (i < nColumns - 1)
                    p.append(",");
            }
            p.append(";");

            //subject column and candidates for subject column
            int subjectColumnID = table_annotation.getSubjectColumn();
            String subjectColumnName = table.getColumnHeader(subjectColumnID).getHeaderText();;
            p.append(subjectColumnName);
            p.append(";");

            int nneColumns = table_annotation.getCandidateNEForSubjectColumn().size();
            p.append(String.valueOf(nneColumns));
            p.append(";");

            //list the candidates (name:score)
            for (ObjObj object : table_annotation.getCandidateNEForSubjectColumn()) {
                int columnID = (int) object.getMainObject();
                subjectColumnName = table.getColumnHeader(columnID).getHeaderText();;
                p.append(subjectColumnName.trim());
                ObjObj otherObject = (ObjObj) object.getOtherObject();
                p.append(":" + otherObject.getMainObject());
                p.append(",");

            }
            p.append("\n");

        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (p != null) {
                p.close();
            }
        }
    }

    /**
     * It writes for each row
     * 
     * @param table
     * @param table_annotation
     * @param cell_key
     * @throws FileNotFoundException
     */
    public void writeClassificationStatisticsCSV(File f, LTable table, LTableAnnotation table_annotation, String cell_key) throws FileNotFoundException {

        boolean prepareHeader = false;
        File temp = new File(cell_key);
        if (!temp.exists()) {
            // do something
            prepareHeader = true;
        }

        Charset encoding = table.getEncoding();

        PrintWriter p = null;
        try {
            p = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cell_key, true), encoding)));

            //        PrintWriter p = new PrintWriter(new FileOutputStream(
            //                new File("cell_key"),
            //                true /* append = true */));

            if (prepareHeader) {
                p.println("File;ClassifiedColumn;TopScore;TopConcept");
            }
            //classification 
            for (ObjObj object : table_annotation.getCandidateNEForSubjectColumn()) {

                int columnID = (int) object.getMainObject();
                String columnName = table.getColumnHeader(columnID).getHeaderText();

                //get classification
                HeaderAnnotation[] anns = table_annotation.getHeaderAnnotation(columnID);
                if (anns != null && anns.length > 0) {
                    StringBuilder s = new StringBuilder();
                    //s.append("\n" + columnName);

                    //get only top concepts (with highest score - more concept can have the highest score!)
                    double prevScore = 0.0;
                    for (HeaderAnnotation ha : anns) {

                        if (prevScore == 0.0) {

                            prevScore = ha.getFinalScore();

                        }

                        if (ha.getFinalScore() < prevScore) {
                            //it is not the highest score, we break (the values are sorted)
                            break;
                        }

                        s.append(f.getName());
                        s.append(";");

                        s.append(columnName.trim());
                        s.append(";");

                        s.append(new DecimalFormat("#.###").format(prevScore));
                        s.append(";");

                        s.append(ha.getAnnotation_url());
                        s.append("\n");
                    }
                    p.append(s.toString());
                }

            }
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (p != null) {
                p.close();
            }
        }
    }

    /**
     * It writes for each row
     * 
     * @param table
     * @param table_annotation
     * @param cell_key
     * @throws FileNotFoundException
     */
    public void writeClassificationStatisticsAllConceptsCSV(File f, LTable table, LTableAnnotation table_annotation, String cell_key) throws FileNotFoundException {

        boolean prepareHeader = false;
        File temp = new File(cell_key);
        if (!temp.exists()) {
            // do something
            prepareHeader = true;
        }

        Charset encoding = table.getEncoding();

        PrintWriter p = null;
        try {
            p = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cell_key, true), encoding)));

            //        PrintWriter p = new PrintWriter(new FileOutputStream(
            //                new File("cell_key"),
            //                true /* append = true */));

            if (prepareHeader) {
                p.println("File;ClassifiedColumn;Score;Concept");
            }
            //classification 
            for (ObjObj object : table_annotation.getCandidateNEForSubjectColumn()) {

                int columnID = (int) object.getMainObject();
                String columnName = table.getColumnHeader(columnID).getHeaderText();

                //get classification
                HeaderAnnotation[] anns = table_annotation.getHeaderAnnotation(columnID);
                if (anns != null && anns.length > 0) {
                    StringBuilder s = new StringBuilder();
                    //s.append("\n" + columnName);

                    //get only top concepts (with highest score - more concept can have the highest score!)
                    for (HeaderAnnotation ha : anns) {

                        s.append(f.getName());
                        s.append(";");

                        s.append(columnName.trim());
                        s.append(";");

                        s.append(new DecimalFormat("#.###").format(ha.getFinalScore()));
                        s.append(";");

                        s.append(ha.getAnnotation_url());
                        s.append("\n");
                    }
                    p.append(s.toString());
                }

            }
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (p != null) {
                p.close();
            }
        }
    }

    protected void writeRelationKeyFile(LTableAnnotation table_annotation, String relation_key) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(relation_key);
        for (Map.Entry<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>> e : table_annotation.getRelationAnnotations_across_columns().entrySet()) {
            int subCol = e.getKey().getSubjectCol();
            int objCol = e.getKey().getObjectCol();
            List<HeaderBinaryRelationAnnotation> relations = e.getValue();
            Collections.sort(relations);
            StringBuilder s = new StringBuilder();
            double prevScore = 0.0;
            for (HeaderBinaryRelationAnnotation hr : relations) {
                if (prevScore == 0.0) {

                    s.append(hr.getAnnotation_url());
                    prevScore = hr.getFinalScore();
                }
                else {
                    if (hr.getFinalScore() == prevScore) {
                        s.append("=").append(hr.getAnnotation_url());
                    }
                    else
                        s.append("|").append(hr.getAnnotation_url());
                }
            }
            p.println(subCol + "," + objCol + "=" + s.toString());
        }
        p.close();
    }

    protected void writeHeaderKeyFile(LTable table, LTableAnnotation table_annotation, String header_key) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(header_key);

        for (int c = 0; c < table.getNumCols(); c++) {
            HeaderAnnotation[] anns = table_annotation.getHeaderAnnotation(c);
            if (anns != null && anns.length > 0) {
                StringBuilder s = new StringBuilder();
                s.append(c).append("=");

                double prevScore = 0.0;
                for (HeaderAnnotation ha : anns) {
                    if (prevScore == 0.0) {

                        s.append(ha.getAnnotation_url());
                        prevScore = ha.getFinalScore();
                    }
                    else {
                        if (ha.getFinalScore() == prevScore) {
                            s.append("=").append(ha.getAnnotation_url());
                        }
                        else
                            s.append("|").append(ha.getAnnotation_url());
                    }
                }
                if (table.getColumnHeader(c).getFeature().getMostDataType().getCandidateType().equals(
                        DataTypeClassifier.DataType.NAMED_ENTITY
                        ))
                    s.append("\t\t\t___NE");
                p.println(s.toString());
            }
        }

        p.close();
    }

    protected void writeTriples(List<LTableTriple> triples, String outFile) throws FileNotFoundException {
        PrintWriter p = new PrintWriter(outFile);
        p.println("<html><body>");
        for (LTableTriple ltt : triples) {
            p.println("<br>&lt;" + ltt.getSubject_annotation() + "," + ltt.getRelation_annotation() + "," + ltt.getObject_annotation() + "&gt;, " +
                    "(" + ltt.getSubject() + "," + ltt.getObject() + "), " + "[" + ltt.getSubject_position()[0] + "," + ltt.getSubject_position()[1] + "][" +
                    ltt.getObject_position()[0] + "," + ltt.getObject_position()[1] + "]</br>");
        }
        p.println("</body></html>");
        p.close();
    }

    protected String writeRelation_inCell(LTable table, LTableAnnotation tab_annotations) {
        StringBuilder out = new StringBuilder();
        out.append("<tr>\n");
        for (int row = 0; row < table.getNumRows(); row++) {
            for (int col = 0; col < table.getNumCols(); col++) {
                String color = col == tab_annotations.getSubjectColumn() ? " bgcolor=\"yellow\"" : "";
                LTableContentCell cell = table.getContentCell(row, col);

                out.append("\t<td").append(color).append(">").append(cell.getText()).append(cell.getOther_text()).append("</td>\n");

                //then annotations
                if (col == tab_annotations.getSubjectColumn()) {
                    out.append("\t<td");
                    StringBuilder annotation = new StringBuilder();
                    CellAnnotation[] cAnns = tab_annotations.getContentCellAnnotations(row, col);
                    if (cAnns == null)
                        annotation.append(">-");
                    else {
                        annotation.append(" bgcolor=\"#00FF00\">");
                        for (int i = 0; i < cAnns.length; i++) {
                            CellAnnotation cAnn = cAnns[i];
                            if (i == 0) { //the winning annotation
                                annotation.append("<br><b>").append(generateCellAnnotationString(cAnn)).append("</b></br>");
                            } else if (showLosingCandidates) { //others
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
                    Key_SubjectCol_ObjectCol key = new Key_SubjectCol_ObjectCol(tab_annotations.getSubjectColumn(), col);
                    Map<Integer, List<CellBinaryRelationAnnotation>> tmp = tab_annotations.getRelationAnnotationsBetween(key.getSubjectCol(), key.getObjectCol());
                    if (tmp == null) {
                        annotation.append(">-");
                        annotation.append("</td>\n");
                        out.append(annotation);
                        continue;
                    }
                    List<CellBinaryRelationAnnotation> crAnns = tmp.get(row);

                    if (crAnns == null)
                        annotation.append(">-");
                    else {
                        Collections.sort(crAnns);
                        annotation.append(" bgcolor=\"#00FF00\">");
                        for (int i = 0; i < crAnns.size(); i++) {
                            CellBinaryRelationAnnotation crAnn = crAnns.get(i);
                            if (i == 0) { //the winning annotation
                                annotation.append("<br><b>").append(generateAcrossCellRelationString(crAnn)).append("</b></br>");
                            } else if (showLosingCandidates) { //others
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

    protected String writeRelation_inHeader(LTable table, LTableAnnotation tab_annotations) {
        StringBuilder out = new StringBuilder();
        out.append("<tr>\n");
        for (int col = 0; col < table.getNumCols(); col++) {
            String color = col == tab_annotations.getSubjectColumn() ? " bgcolor=\"yellow\"" : "";
            LTableColumnHeader header = table.getColumnHeader(col);
            if (header == null)
                continue;
            out.append("\t<th").append(color).append(">").append(header.getHeaderText()).append("</th>\n");

            //then annotations
            if (col == tab_annotations.getSubjectColumn()) {
                out.append("\t<th");
                StringBuilder annotation = new StringBuilder();
                HeaderAnnotation[] hAnns = tab_annotations.getHeaderAnnotation(col);
                if (hAnns == null)
                    annotation.append(">-");
                else {
                    annotation.append(" bgcolor=\"#00FF00\">");
                    double best_score = 0.0;
                    for (int i = 0; i < hAnns.length; i++) {
                        HeaderAnnotation hAnn = hAnns[i];
                        if (i == 0) { //the winning annotation
                            annotation.append("<br><b>").append(generateHeaderAnnotationString(hAnn)).append("</b></br>");
                            best_score = hAnn.getFinalScore();
                        } else if (hAnn.getFinalScore() == best_score) {
                            annotation.append("<br><b>").append(generateHeaderAnnotationString(hAnn)).append("</b></br>");
                        } else if (showLosingCandidates) { //others
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
                Key_SubjectCol_ObjectCol key = new Key_SubjectCol_ObjectCol(tab_annotations.getSubjectColumn(), col);
                List<HeaderBinaryRelationAnnotation> hAnns = tab_annotations.getRelationAnnotations_across_columns().get(key);

                if (hAnns == null)
                    annotation.append(">-");
                else {
                    Collections.sort(hAnns);
                    annotation.append(" bgcolor=\"#00FF00\">");
                    for (int i = 0; i < hAnns.size(); i++) {
                        HeaderBinaryRelationAnnotation hAnn = hAnns.get(i);
                        if (i == 0) { //the winning annotation
                            annotation.append("<br><b>").append(generateAcrossHeaderRelationString(hAnn)).append("</b></br>");
                        } else if (showLosingCandidates) { //others
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

    protected String writeHeader(LTable table, LTableAnnotation tab_annotations) {
        StringBuilder out = new StringBuilder();
        out.append("<tr>\n");
        for (int col = 0; col < table.getNumCols(); col++) {
            LTableColumnHeader header = table.getColumnHeader(col);
            if (header == null)
                continue;
            out.append("\t<th>").append(header.getHeaderText()).append("</th>\n");

            //then annotations
            out.append("\t<th");
            StringBuilder annotation = new StringBuilder();
            HeaderAnnotation[] hAnns = tab_annotations.getHeaderAnnotation(col);
            if (hAnns == null)
                annotation.append(">-");
            else {
                annotation.append(" bgcolor=\"#00FF00\">");
                double best_score = 0.0;
                for (int i = 0; i < hAnns.length; i++) {
                    HeaderAnnotation hAnn = hAnns[i];
                    if (i == 0) { //the winning annotation
                        annotation.append("<br><b>").append(generateHeaderAnnotationString(hAnn)).append("</b></br>");
                        best_score = hAnn.getFinalScore();
                    } else if (hAnn.getFinalScore() == best_score) {
                        annotation.append("<br><b>").append(generateHeaderAnnotationString(hAnn)).append("</b></br>");
                    } else if (showLosingCandidates) { //others
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

    protected String writeCell(LTable table, LTableAnnotation tab_annotations) {
        StringBuilder out = new StringBuilder();

        for (int row = 0; row < table.getNumRows(); row++) {
            out.append("<tr>\n");
            for (int col = 0; col < table.getNumCols(); col++) {
                LTableContentCell tcc = table.getContentCell(row, col);
                out.append("\t<td>").append(tcc.getText()).append(tcc.getOther_text()).append("<font color=\"grey\">").append(" [").
                        append(tcc.getType().getValue()).append("]</font>").
                        append("</td>\n");

                //then annotations
                out.append("\t<td");
                StringBuilder annotation = new StringBuilder();
                CellAnnotation[] cAnns = tab_annotations.getContentCellAnnotations(row, col);
                if (cAnns == null)
                    annotation.append(">-");
                else {
                    annotation.append(" bgcolor=\"#00FF00\">");
                    for (int i = 0; i < cAnns.length; i++) {
                        CellAnnotation cAnn = cAnns[i];
                        if (i == 0) { //the winning annotation
                            annotation.append("<br><b>").append(generateCellAnnotationString(cAnn)).append("</b></br>");
                        } else if (showLosingCandidates) { //others
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

    protected Object generateCellAnnotationString(CellAnnotation cAnn) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"" + linkPrefix + cAnn.getAnnotation().getId() + "\">").
                append(cAnn.getAnnotation().getName()).append("</a>").
                append("=").append(Math.round(cAnn.getFinalScore() * 100.0) / 100.0).append(cAnn.getAnnotation().getTypeIds());
        return sb.toString();
    }

    protected String generateHeaderAnnotationString(HeaderAnnotation ha) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"" + linkPrefix + ha.getAnnotation_url() + "\">").
                append(ha.getAnnotation_url()).append("(").append(ha.getAnnotation_label()).append(")</a>").
                append("=").append(Math.round(ha.getFinalScore() * 100.0) / 100.0).append(ha.getSupportingRows());
        return sb.toString();
    }

    protected String generateAcrossHeaderRelationString(HeaderBinaryRelationAnnotation ha) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"" + linkPrefix + ha.getAnnotation_url() + "\">").
                append(ha.getAnnotation_url()).
                append("</a>").
                append("=").append(Math.round(ha.getFinalScore() * 100.0) / 100.0).append(ha.getSupportingRows());
        return sb.toString();
    }

    protected String generateAcrossCellRelationString(CellBinaryRelationAnnotation ca) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"" + linkPrefix + ca.getAnnotation_url() + "\">").
                append(ca.getAnnotation_url()).
                append("</a>").
                append("=").append(Math.round(ca.getScore() * 100.0) / 100.0);
        return sb.toString();
    }

}

package uk.ac.shef.dcs.sti.algorithm.smp;

import uk.ac.shef.dcs.sti.algorithm.tm.TripleGenerator;
import uk.ac.shef.dcs.sti.io.TAnnotationWriter;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.sti.rep.HeaderAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.sti.rep.LTableAnnotation;
import uk.ac.shef.dcs.sti.rep.TColumnHeader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by zqz on 29/04/2015.
 */
public class TAnnotationWriter_SMP extends TAnnotationWriter {
    public TAnnotationWriter_SMP(TripleGenerator tripleGenerator) {
        super(tripleGenerator);
    }

    protected void writeHeaderKeyFile(Table table, LTableAnnotation table_annotation, String header_key) throws FileNotFoundException {
        if (!(table_annotation instanceof LTableAnnotation_SMP_Freebase))
            super.writeHeaderKeyFile(table, table_annotation, header_key);
        else {
            PrintWriter p = new PrintWriter(header_key);

            for (int c = 0; c < table.getNumCols(); c++) {
                HeaderAnnotation[] anns = table_annotation.getHeaderAnnotation(c);
                if (anns != null && anns.length > 0) {
                    StringBuilder s = new StringBuilder();
                    s.append(c).append("=");

                    double prevScore = 0.0;
                    double prevGranularity = 0.0;
                    for (HeaderAnnotation ha : anns) {
                        if (prevScore == 0.0) {
                            s.append(ha.getAnnotation_url());
                            prevScore = ha.getFinalScore();
                            prevGranularity = ha.getScoreElements().get(ColumnClassifier.SMP_SCORE_GRANULARITY);
                        } else {
                            if (ha.getFinalScore() == prevScore && ha.getScoreElements().get(ColumnClassifier.SMP_SCORE_GRANULARITY)==prevGranularity) {
                                s.append("=").append(ha.getAnnotation_url());
                            } else
                                s.append("|").append(ha.getAnnotation_url());
                        }
                    }
                    if (table.getColumnHeader(c).getFeature().getMostFrequentDataType().getType().equals(
                            DataTypeClassifier.DataType.NAMED_ENTITY
                    ))
                        s.append("\t\t\t___NE");
                    p.println(s.toString());
                }
            }

            p.close();
        }
    }

    protected String writeHeader(Table table, LTableAnnotation tab_annotations) {
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
            HeaderAnnotation[] hAnns = tab_annotations.getHeaderAnnotation(col);
            if (hAnns == null)
                annotation.append(">-");
            else {
                annotation.append(" bgcolor=\"#00FF00\">");
                double best_score = 0.0, best_granularity_score=0.0;
                for (int i = 0; i < hAnns.length; i++) {
                    HeaderAnnotation hAnn = hAnns[i];
                    if (i == 0) { //the winning annotation
                        annotation.append("<br><b>").append(generateHeaderAnnotationString(hAnn)).append("</b></br>");
                        best_score = hAnn.getFinalScore();
                        best_granularity_score=hAnn.getScoreElements().get(
                                ColumnClassifier.SMP_SCORE_GRANULARITY
                        );
                    } else if (hAnn.getFinalScore() == best_score &&
                            hAnn.getScoreElements().get(
                                    ColumnClassifier.SMP_SCORE_GRANULARITY)==best_granularity_score) {
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
}

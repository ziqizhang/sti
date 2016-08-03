package uk.ac.shef.dcs.sti.core.algorithm.smp;

import uk.ac.shef.dcs.sti.util.TripleGenerator;
import uk.ac.shef.dcs.sti.io.TAnnotationWriter;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by zqz on 29/04/2015.
 */
public class TAnnotationWriterSMP extends TAnnotationWriter {
    public TAnnotationWriterSMP(TripleGenerator tripleGenerator) {
        super(tripleGenerator);
    }

    protected void writeHeaderKeyFile(Table table, TAnnotation table_annotation, String header_key) throws FileNotFoundException {
        if (!(table_annotation instanceof TAnnotationSMPFreebase))
            super.writeHeaderKeyFile(table, table_annotation, header_key);
        else {
            PrintWriter p = new PrintWriter(header_key);

            for (int c = 0; c < table.getNumCols(); c++) {
                TColumnHeaderAnnotation[] anns = table_annotation.getHeaderAnnotation(c);
                if (anns != null && anns.length > 0) {
                    StringBuilder s = new StringBuilder();
                    s.append(c).append("=");

                    double prevScore = 0.0;
                    double prevGranularity = 0.0;
                    for (TColumnHeaderAnnotation ha : anns) {
                        if (prevScore == 0.0) {
                            s.append(ha.getAnnotation().getId());
                            prevScore = ha.getFinalScore();
                            prevGranularity = ha.getScoreElements().get(TColumnClassifier.SMP_SCORE_GRANULARITY);
                        } else {
                            if (ha.getFinalScore() == prevScore && ha.getScoreElements().get(TColumnClassifier.SMP_SCORE_GRANULARITY)==prevGranularity) {
                                s.append("=").append(ha.getAnnotation().getId());
                            } else
                                s.append("|").append(ha.getAnnotation().getId());
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
                double best_score = 0.0, best_granularity_score=0.0;
                for (int i = 0; i < hAnns.length; i++) {
                    TColumnHeaderAnnotation hAnn = hAnns[i];
                    if (i == 0) { //the winning annotation
                        annotation.append("<br><b>").append(generateHeaderAnnotationString(hAnn)).append("</b></br>");
                        best_score = hAnn.getFinalScore();
                        best_granularity_score=hAnn.getScoreElements().get(
                                TColumnClassifier.SMP_SCORE_GRANULARITY
                        );
                    } else if (hAnn.getFinalScore() == best_score &&
                            hAnn.getScoreElements().get(
                                    TColumnClassifier.SMP_SCORE_GRANULARITY)==best_granularity_score) {
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

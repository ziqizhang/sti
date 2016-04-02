package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.sti.rep.TAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;

import java.util.List;
import java.util.Set;

/**
 * Created by - on 02/04/2016.
 */
public class RELATIONENUMERATION {
    private static final Logger LOG = Logger.getLogger(RELATIONENUMERATION.class.getName());

    public void enumerate(List<Pair<Integer, Pair<Double, Boolean>>> subjectColCandidadteScores,
                          Set<Integer> ignoreCols,
                          TColumnColumnRelationEnumerator relationEnumerator,
                          TAnnotation tableAnnotations,
                          Table table){
        double winningSolutionScore = 0;
        int subjectCol = -1;
        TAnnotation winningSolution = null;
        LOG.info("\t RELATION ENUMERATION begins");
        for (Pair<Integer, Pair<Double, Boolean>> mainCol : subjectColCandidadteScores) {
            //tab_annotations = new TAnnotation(table.getNumRows(), table.getNumCols());
            subjectCol = mainCol.getKey();
            if (ignoreCols.contains(subjectCol)) continue;

            LOG.info(">>\t\t Let subject column=" + subjectCol);
            int relatedColumns =
                    relationEnumerator.runRelationEnumeration(tableAnnotations, table, subjectCol);
            todo continue here......
            boolean interpretable = false;
            if (relatedColumns > 0) {
                interpretable = true;
            }
            if (interpretable) {
                tableAnnotations.setSubjectColumn(subjectCol);
                break;
            } else {
                //the current subject column could be wrong, try differently
                double overall_score_of_current_solution = scoreSolution(tableAnnotations, table, subjectCol);
                if (overall_score_of_current_solution > winningSolutionScore) {
                    tableAnnotations.setSubjectColumn(subjectCol);
                    winningSolution = tableAnnotations;
                    winningSolutionScore = overall_score_of_current_solution;
                }
                tableAnnotations.resetRelationAnnotations();
                LOG.error(">>\t Main column does not satisfy number of relations check, continue to the next main column...");
                continue;
            }
        }
        if (tableAnnotations == null && winningSolution != null) {
            tableAnnotations = winningSolution;
        }

        if (TableMinerConstants.REVISE_HBR_BY_DC && update != null) {
            List<String> domain_rep = update.createDomainRep(table, tableAnnotations, annotatedColumns);
            revise_header_binary_relations(tableAnnotations, domain_rep);
        }
    }
}

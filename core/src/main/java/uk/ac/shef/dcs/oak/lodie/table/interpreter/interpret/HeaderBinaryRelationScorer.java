package uk.ac.shef.dcs.oak.lodie.table.interpreter.interpret;

import uk.ac.shef.dcs.oak.lodie.table.rep.CellBinaryRelationAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.HeaderAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.HeaderBinaryRelationAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 27/02/14
 * Time: 12:34
 * To change this template use File | Settings | File Templates.
 */
public interface HeaderBinaryRelationScorer {
    Set<HeaderBinaryRelationAnnotation> score(List<CellBinaryRelationAnnotation> input_from_row,
                                              Set<HeaderBinaryRelationAnnotation> header_binary_relations_prev,
                                              int subjectCol, int objectCol,
                                              LTable table);

    Map<String, Double> compute_final_score(HeaderBinaryRelationAnnotation ha, int tableRowsTotal);
    Set<String> create_annotation_bow(HeaderBinaryRelationAnnotation hbr, boolean lowercase, boolean discard_single_char);
    double score_domain_consensus(HeaderBinaryRelationAnnotation hbr, List<String> domain_representation);
}

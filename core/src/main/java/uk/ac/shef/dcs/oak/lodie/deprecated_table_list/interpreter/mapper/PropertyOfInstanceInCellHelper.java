package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.mapper;

import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.filter.FilterPolicy;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.CandidateFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.label.LabelFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.property.PredicateOfObjectAndPredDomainFinderSparql;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 30/04/13
 * Time: 13:14
 */
class PropertyOfInstanceInCellHelper {

    public static ObjectMatrix2D findCandidateProperties_and_Domains_and_Labels_ForHeadersByDataValues(
            Logger log,
            LTable table,
            ObjectMatrix2D cell_candidate_asInstance,
            List<Integer> dataColumns,
            Map<String, Set<String>> all_labels,
            Map<String, String> mapOfEquivalence,
            Map<String, String> props2Domains,
            PredicateOfObjectAndPredDomainFinderSparql[] predicateOfObjectFinder,
            LabelFinder[] labelFinder_forHeaderProperties,
            FilterPolicy[] policies) {
        ObjectMatrix2D headerCandidates = new SparseObjectMatrix2D(cell_candidate_asInstance.rows(), cell_candidate_asInstance.columns());
        log.info("\ttable of size:r=" + cell_candidate_asInstance.rows() + ",c=" + cell_candidate_asInstance.columns());
        Map<String, Set<String>> inMemCache = new HashMap<String, Set<String>>();
        Map<String, Integer> properties2CountsForThisTable = new HashMap<String, Integer>();  //

        Set<String> allUniqueProperties = new HashSet<String>();
        for (int c : dataColumns) {
            if (TableContent2KnowledgeBaseMapper.skip(table, -1, c, policies))
                continue;

            for (int r = 1; r < cell_candidate_asInstance.rows(); r++) {
                log.info("\t\\r=" + r + " c=" + c);
                Object cell = cell_candidate_asInstance.get(r, c);
                if (cell == null)
                    continue;
                Set<String> uris = (Set<String>) cell;
                Map<String, Set<String>> propertiesForThisCell = new HashMap<String, Set<String>>();

                for (String uri : uris) {
                    Set<String> propertiesOfUri = inMemCache.get(uri);
                    if (propertiesOfUri == null) {
                        propertiesOfUri = new HashSet<String>();

                        for (CandidateFinder finder : predicateOfObjectFinder) {
                            try {
                                List<String[]> predicates =(List<String[]>) finder.findCandidates("<" + uri + ">");
                                if (predicates == null || predicates.size() == 0)
                                    continue;

                                for (String[] p : predicates) {
                                    String prop = p[0];
                                    String domain = p[1];
                                    String equivD = p[2];

                                    props2Domains.put(prop, domain);
                                    if (equivD.length() > 0) {
                                        mapOfEquivalence.put(domain, equivD);
                                        mapOfEquivalence.put(equivD, domain);
                                        props2Domains.put(prop, equivD);
                                    }
                                    propertiesOfUri.add(prop);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    propertiesForThisCell.put(uri, propertiesOfUri);
                    inMemCache.put(uri, propertiesOfUri);
                    allUniqueProperties.addAll(propertiesOfUri);
                }
                headerCandidates.set(r, c, propertiesForThisCell);

                //counting
                Set<String> uniquePropsForThisCell = new HashSet<String>();
                for (Set<String> v : propertiesForThisCell.values())
                    uniquePropsForThisCell.addAll(v);
                for (String up : uniquePropsForThisCell) {
                    Integer count = properties2CountsForThisTable.get(up);
                    if (count == null)
                        count = 0;
                    count = count + 1;
                    properties2CountsForThisTable.put(up, count);
                }
            }
        }

        //after counting, discard infrequent predicates
        int before = allUniqueProperties.size();
        for (Map.Entry<String, Integer> e : properties2CountsForThisTable.entrySet()) {
            if (e.getValue() < 2) {
                props2Domains.remove(e.getKey());
                allUniqueProperties.remove(e.getKey());
            }
        }
        for (int c = 0; c < headerCandidates.columns(); c++) {
            for (int r = 1; r < headerCandidates.rows(); r++) {
                Object cell = headerCandidates.get(r, c);
                if (cell == null)
                    continue;
                Map<String, Set<String>> propsOfThisCell = (Map<String, Set<String>>) cell;

                for (Set<String> supers : propsOfThisCell.values()) {
                    Iterator<String> it = supers.iterator();
                    while (it.hasNext()) {
                        String p = it.next();
                        Integer freq = properties2CountsForThisTable.get(p);
                        if (freq == null || freq < 2)
                            it.remove();
                    }
                }
            }
        }
        log.info("\t\\ before discarding infrequent predicates:" + before + ", after:" + props2Domains.size());

        TableContent2KnowledgeBaseMapper.findLabelsForResources(
                all_labels, mapOfEquivalence,
                allUniqueProperties, labelFinder_forHeaderProperties);
        return headerCandidates;
    }
}

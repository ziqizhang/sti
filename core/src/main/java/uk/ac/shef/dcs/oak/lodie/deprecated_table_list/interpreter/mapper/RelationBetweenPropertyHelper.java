package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.mapper;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.LTableColumn2ColumnRelation;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.TripleInTableRow;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.CandidateFinder;

import java.util.*;
import java.util.logging.Logger;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 03/05/13
 * Time: 18:30
 */
class RelationBetweenPropertyHelper {
    public static List<LTableColumn2ColumnRelation> findRelationsBetweenHeaders_ByDataCellProperties(
            Logger log,
            ObjectMatrix2D candidates,
            Map<String, String> prop2Domain,
            List<Integer> dataColumns,
            int mainSubjectColumn,
            CandidateFinder... domainOfPropFinders) {

        List<LTableColumn2ColumnRelation> result = new ArrayList<LTableColumn2ColumnRelation>();
        for (int c : dataColumns) {
            if (c == mainSubjectColumn)
                continue;

            LTableColumn2ColumnRelation tccr = new LTableColumn2ColumnRelation(mainSubjectColumn, c);

            for (int r = 1; r < candidates.rows(); r++) {
                Map<String, Set<String>> main = (Map<String, Set<String>>) candidates.get(r, mainSubjectColumn);
                Map<String, Set<String>> other = (Map<String, Set<String>>) candidates.get(r, c);
                if (main == null || other == null)
                    continue;

                //each cell may have multiple candidates, retrieve them all
                Set<String> mainCandidates = new HashSet<String>();
                Set<String> otherCandidates = new HashSet<String>();
                for (Set<String> v : main.values())
                    mainCandidates.addAll(v);
                for (Set<String> v : other.values())
                    otherCandidates.addAll(v);
                log.info("\t\\r=" + r + ", mainC=" + mainSubjectColumn + ", c=" + c + "; size mainC=" + mainCandidates.size() + ", size c=" + otherCandidates.size());

                findSharedDomainsBetweenProps(
                        mainCandidates, otherCandidates, c, r, mainSubjectColumn, tccr, prop2Domain, domainOfPropFinders);

            }//end for ecah row
            result.add(tccr);
        } //end for ecah datacolumn

        return result;
    }


    static List<LTableColumn2ColumnRelation> findRelationsBetweenHeaders_By_Properties(
            ObjectMatrix1D candidates,
            Map<String, String> prop2Domain,
            List<Integer> dataColumns,
            int mainSubjectColumn,
            CandidateFinder... domainOfPropFinder) {

        List<LTableColumn2ColumnRelation> result = new ArrayList<LTableColumn2ColumnRelation>();
        for (int c : dataColumns) {
            if (c == mainSubjectColumn)
                continue;

            LTableColumn2ColumnRelation tccr = new LTableColumn2ColumnRelation(mainSubjectColumn, c);

            Set<String> main = (Set<String>) candidates.get(mainSubjectColumn);
            Set<String> other = (Set<String>) candidates.get(c);
            if (main == null || other == null)
                continue;

            //each cell may have multiple candidates, retrieve them all
            findSharedDomainsBetweenProps(
                    main, other, c, 0, mainSubjectColumn, tccr, prop2Domain, domainOfPropFinder);

            result.add(tccr);
        } //end for ecah datacolumn

        return result;
    }

    private static void findSharedDomainsBetweenProps(Set<String> mainCandidates,
                                                      Set<String> otherCandidates,
                                                      int c, int r,
                                                      int mainSubjectColumn,
                                                      LTableColumn2ColumnRelation tccr,
                                                      Map<String, String> prop2Domain,
                                                      CandidateFinder... domainOfPropFinder)

    {
        //get all pairs b/w the two lists and their relations, create triples accordingly
        for (String m : mainCandidates) {
            for (String o : otherCandidates) {
                Set<String> relations = new HashSet<String>();
                boolean relationReverse = false;
                //find relations that link m as subject and o as object
                String domainOfM = prop2Domain.get(m);
                String domainOfO = prop2Domain.get(o);

                if (domainOfM == null) {
                    for (CandidateFinder f : domainOfPropFinder) {
                        List<String> found = null;
                        try {
                            found = (List<String>)f.findCandidates(m);
                        } catch (Exception e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        if (found != null && found.size() > 0) {
                            domainOfM = found.get(0);
                            break;
                        }
                    }
                    domainOfM = domainOfM == null ? "" : domainOfM;
                    prop2Domain.put(m, domainOfM);
                }
                if (domainOfO == null) {
                    for (CandidateFinder f : domainOfPropFinder) {
                        List<String> found = null;
                        try {
                            found = f.findCandidates(m);
                        } catch (Exception e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        if (found != null && found.size() > 0) {
                            domainOfM = found.get(0);
                            break;
                        }
                    }
                    domainOfO = domainOfO == null ? "" : domainOfO;
                    prop2Domain.put(o, domainOfO);
                }


                if (domainOfM != null && !domainOfM.equals("") && domainOfM.equals(domainOfO)) {
                    relations.add(domainOfM);
                    continue;
                }

                for (String rel : relations) {
                    TripleInTableRow triple = new TripleInTableRow(m, rel, o, mainSubjectColumn, c, r);
                    triple.setPredicateReverse(relationReverse);
                    tccr.addTriple(triple);
                }
            }//end for each candidate uri for the *object* cell content
        }//end for each candidate uri for the *subject* cell content
    }
}

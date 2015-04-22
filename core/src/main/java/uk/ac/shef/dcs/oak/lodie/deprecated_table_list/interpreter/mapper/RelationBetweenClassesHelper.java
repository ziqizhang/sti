package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.mapper;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.LTableColumn2ColumnRelation;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.TripleInTableRow;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.relation.RelationFinder;
import uk.ac.shef.dcs.oak.util.PairOfObjects;

import java.util.*;
import java.util.logging.Logger;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 01/05/13
 * Time: 21:08
 */
class RelationBetweenClassesHelper {
    /**
     * //todo: currently we only find relations with *main*subject columns. not other column pairs
     * A BRUTE FORCE APPROACH to find candidate relations between columns
     *
     * @param candidates  - candidate uris for each value in each cell of a table
     * @param -           the LTable object to be interpreted
     * @param dataColumns - integer ids indicating which columns of the table contain data to be interpreted
     */
    static List<LTableColumn2ColumnRelation> findRelationsBetweenHeaders_ByDataCellSuperclasses(
            Logger log,
            ObjectMatrix2D candidates,
            List<Integer> dataColumns,
            int mainSubjectColumn,
            RelationFinder... relationFinder) {
        Map<String, Set<String>> inMemCache = new HashMap<String, Set<String>>();
        Set<PairOfObjects<String>> pairs = new HashSet<PairOfObjects<String>>();

        List<LTableColumn2ColumnRelation> result = new ArrayList<LTableColumn2ColumnRelation>();
        Set<String> allMainInColumn = new HashSet<String>(), allOtherInColumn = new HashSet<String>();
        for (int c : dataColumns) {
            if (c == mainSubjectColumn)
                continue;

            for (int r = 1; r < candidates.rows(); r++) {
                Map<String, Set<String>> main = (Map<String, Set<String>>) candidates.get(r, mainSubjectColumn);
                Map<String, Set<String>> other = (Map<String, Set<String>>) candidates.get(r, c);
                //each cell may have multiple candidates, retrieve them all
                Set<String> allMainInCell = new HashSet<String>(), allOtherInCell = new HashSet<String>();
                for (Set<String> v : main.values()) {
                    allMainInCell.addAll(v);
                    allMainInColumn.addAll(allMainInCell);
                }
                for (Set<String> v : other.values()) {
                    allOtherInCell.addAll(v);
                    allOtherInColumn.addAll(allOtherInCell);
                }

                for (String a : allMainInCell) {
                    for (String o : allOtherInCell) {
                        if (!a.equals(o)) {
                            //todo: if want to discard cross onto relations, filter here (check ns of a and o)
                            pairs.add(new PairOfObjects<String>(a, o));
                        }
                    }
                }
            }
        }
        log.info("\t all possible pairs =" + pairs.size());//now find all possible relations
        //////////////////////////////
        /*List<PairOfObjects<String>> all = new ArrayList<PairOfObjects<String>>(pairs);
        Collections.sort(all, new Comparator<PairOfObjects<String>>() {

            @Override
            public int compare(PairOfObjects<String> o1, PairOfObjects<String> o2) {
                int c = o1.getFirst().compareTo(o2.getFirst());
                if (c == 0)
                    return o1.getSecond().compareTo(o2.getSecond());
                return c;
            }
        });
        for (PairOfObjects<String> p : all)
            System.out.println(p.getFirst() + ", " + p.getSecond());

        List<String> allMainInColumnAsList = new ArrayList<String>(allMainInColumn);
        Collections.sort(allMainInColumnAsList);
        System.out.println("//////////////////\n\n\n\n\n");
        for (String mainC : allMainInColumnAsList)
            System.out.println(mainC);
        List<String> allOtherInColumnAsList = new ArrayList<String>(allOtherInColumn);
        Collections.sort(allOtherInColumnAsList);
        System.out.println("/////////////////\n\n\n\n\n");
        for (String otherC : allOtherInColumn)
            System.out.println(otherC);*/

        /////////////////////////////////
        findAndCacheRelations(pairs, inMemCache, relationFinder);


        //then lets process each data cell
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

                findRelationsFromCache(mainCandidates, otherCandidates, c, r, mainSubjectColumn, tccr, inMemCache);
            }//end for ecah row

            result.add(tccr);
        } //end for ecah datacolumn
        return result;
    }


    public static List<LTableColumn2ColumnRelation> findRelationsBetweenHeaders_By_Concepts(
            Logger log,
            ObjectMatrix1D header_candidate_asConcept,
            List<Integer> dataColumns, int mainSubjectColumn, RelationFinder[] relationFinderBetweenURIs) {
        Map<String, Set<String>> inMemCache = new HashMap<String, Set<String>>();

        List<LTableColumn2ColumnRelation> result = new ArrayList<LTableColumn2ColumnRelation>();
        for (int c : dataColumns) {
            if (c == mainSubjectColumn)
                continue;

            Object main = header_candidate_asConcept.get(mainSubjectColumn);
            Object other = header_candidate_asConcept.get(c);

            if (main == null || other == null)
                continue;

            LTableColumn2ColumnRelation tccr = new LTableColumn2ColumnRelation(mainSubjectColumn, c);
            //each cell may have multiple candidates, retrieve them all
            Set<String> mainCandidates = (HashSet<String>) main;
            Set<String> otherCandidates = (HashSet<String>) other;
            findAndCacheRelations(mainCandidates, otherCandidates, inMemCache, relationFinderBetweenURIs);

            log.info("\t\\mainC=" + mainSubjectColumn + ", c=" + c + "; size mainC=" + mainCandidates.size() + ", size c=" + otherCandidates.size());

            //get all pairs b/w the two lists and their relations, create triples accordingly
            findRelationsFromCache(mainCandidates, otherCandidates, c, 0, mainSubjectColumn, tccr, inMemCache);

            result.add(tccr);
        } //end for ecah datacolumn
        return result;
    }


    static void findAndCacheRelations(Set<PairOfObjects<String>> pairs,
                                      Map<String, Set<String>> inMemCache,
                                      RelationFinder... relationFinder)

    {
        //get all pairs b/w the two lists and their relations, create triples accordingly
        for (PairOfObjects<String> par : pairs) {

            Set<String> relations = inMemCache.get(par.getFirst() + par.getSecond());
            if (relations == null) {
                relations = new HashSet<String>();
                //find relations that link m as subject and o as object
                for (RelationFinder finder : relationFinder) {
                    List<String> rels = null;
                    try {
                        rels = finder.findRelationBetween(par.getFirst(), par.getSecond());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (rels == null)
                        rels = new ArrayList<String>();
                    if (rels.size() > 0)
                        System.out.println(par.getFirst() + "," + par.getSecond() + "," + rels);
                    relations.addAll(rels);
                    inMemCache.put(par.getFirst() + par.getSecond(), relations);
                }
            }


        }//end for each candidate uri for the *subject* cell content
    }

    static void findAndCacheRelations(Set<String> mainCandidates,
                                              Set<String> otherCandidates,
                                              Map<String, Set<String>> inMemCache,
                                              RelationFinder... relationFinder)

    {
        //get all pairs b/w the two lists and their relations, create triples accordingly
        for (String m : mainCandidates) {
            for (String o : otherCandidates) {
                Set<String> relations = inMemCache.get(m + o);
                if (relations == null) {
                    relations = new HashSet<String>();
                    //find relations that link m as subject and o as object
                    for (RelationFinder finder : relationFinder) {
                        List<String> rels = null;
                        try {
                            rels = finder.findRelationBetween(m, o);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (rels == null)
                            rels = new ArrayList<String>();

                        relations.addAll(rels);
                        inMemCache.put(m + o, relations);
                    }
                }

            }//end for each candidate uri for the *object* cell content
        }//end for each candidate uri for the *subject* cell content
    }

    static void findRelationsFromCache(Set<String> mainCandidates,
                                       Set<String> otherCandidates,
                                       int mainSubjectColumn, int c, int r,
                                       LTableColumn2ColumnRelation tccr,
                                       Map<String, Set<String>> inMemCache)

    {
        //get all pairs b/w the two lists and their relations, create triples accordingly
        for (String m : mainCandidates) {
            for (String o : otherCandidates) {
                Set<String> rels = inMemCache.get(m + o);
                if (rels != null && rels.size() != 0) {
                    for (String rel : rels) {
                        TripleInTableRow triple = new TripleInTableRow(m, rel, o, mainSubjectColumn, c, r);
                        tccr.addTriple(triple);
                    }
                }

            }//end for each candidate uri for the *object* cell content

        }//end for each candidate uri for the *subject* cell content
    }
}

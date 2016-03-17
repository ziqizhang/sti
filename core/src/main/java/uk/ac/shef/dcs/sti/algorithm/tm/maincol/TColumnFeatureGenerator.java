package uk.ac.shef.dcs.sti.algorithm.tm.maincol;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.sti.algorithm.tm.sampler.TContentRowRanker;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.sti.algorithm.tm.stopping.StoppingCriteria;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.sti.rep.LTableColumnHeader;
import uk.ac.shef.dcs.sti.rep.LTableContentCell;
import uk.ac.shef.dcs.util.StringUtils;
import uk.ac.shef.dcs.websearch.bing.v2.APIKeysDepletedException;
import uk.ac.shef.dcs.websearch.bing.v2.BingSearch;

import java.io.IOException;
import java.util.*;

/**

 */
public class TColumnFeatureGenerator {
    private HeaderAndContextMatcher ctxMatcher;
    private HeaderWebsearchMatcher_token websearchMatcher;
    private NLPTools nlpTools;


    public TColumnFeatureGenerator(EmbeddedSolrServer cache, String nlpResource,
                                   List<String> stopWords,
                                   String webSearchPropFile) throws IOException {
        ctxMatcher = new HeaderAndContextMatcher(nlpResource);
        websearchMatcher = new HeaderWebsearchMatcher_token(new HeaderWebsearchMatcherCache(cache),
                new BingSearch(webSearchPropFile),stopWords);
        nlpTools = NLPTools.getInstance(nlpResource);
    }

    //
    public void feature_countEmptyCells(List<ColumnFeature> features, Table table) {
        for (ColumnFeature cf : features) {
            int col = cf.getColId();

            int countEmpty = 0;
            for (int r = 0; r < table.getNumRows(); r++) {
                String textContent = table.getContentCell(r, col).getText();
                if (textContent == null || textContent.length() == 0)
                    countEmpty++;
            }
            cf.setEmptyCells(countEmpty);
        }
    }

    public static void feature_columnDataTypes(Table table) {
        for (int col = 0; col < table.getNumCols(); col++) {
            List<ColumnDataType> types = new ArrayList<ColumnDataType>();
            List<String> candidate_ordered_number = new ArrayList<String>();

            for (int row = 0; row < table.getNumRows(); row++) {
                LTableContentCell tcc = table.getContentCell(row, col);
                String textContent = tcc.getText();
                if (textContent != null) {
                    DataTypeClassifier.DataType dt = DataTypeClassifier.classify(textContent);
                    /*if(dt.equals(DataTypeClassifier.DataType.SHORT_TEXT))
                        System.out.println();*/
                    tcc.setType(dt);
                    if (dt.equals(DataTypeClassifier.DataType.NUMBER))
                        candidate_ordered_number.add(StringUtils.toAlphaNumericWhitechar(textContent).trim());

                    ColumnDataType cdt = new ColumnDataType(dt, 1);
                    int index = types.indexOf(cdt);
                    if (index != -1) {
                        cdt = types.get(index);
                        cdt.setCountRows(cdt.getCountRows() + 1);
                    } else {
                        //ColumnDataType cdt = new ColumnDataType(dt, 1);
                        types.add(cdt);
                    }
                }
            }

            //if any row is PARAGRAPH, it overwrites all other rows the column can only te long paragraph

            Collections.sort(types);
            //check if the column contains ordered numbers?
            if (candidate_ordered_number.size() != 0 && types.get(0).getCandidateType().equals(DataTypeClassifier.DataType.NUMBER)) {
                boolean ordered = DataTypeClassifier.isOrderedNumber(candidate_ordered_number.toArray(new String[0]));
                if (ordered) {
                    types.clear();
                    types.add(new ColumnDataType(DataTypeClassifier.DataType.ORDERED_NUMBER, candidate_ordered_number.size()));
                }
            }

            table.getColumnHeader(col).setType(types);
        }
    }

    //the most frequently found datatype for each column
    public static void feature_mostDataType(List<ColumnFeature> features, Table table) {
        for (ColumnFeature cf : features) {
            int col = cf.getColId();
            LTableColumnHeader header = table.getColumnHeader(col);
            List<ColumnDataType> types = header.getTypes();
            Collections.sort(types);
            cf.setMostDataType(types.get(0));
        }
    }

    //which column is the first NE column
    public void feature_isFirstNEColumn(List<ColumnFeature> features) {
        for (ColumnFeature cf : features) {
            if (cf.getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY)) {
                cf.setFirstNEColumn(true);
                break;
            }
        }
    }

    //which column is the only one NE column in the table
    public int feature_isTheOnlyNEColumn(List<ColumnFeature> features) {
        List<Integer> indexes = new ArrayList<Integer>();

        for (int i = 0; i < features.size(); i++) {
            if (features.get(i).getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY)) {
                indexes.add(i);
            }
        }
        if (indexes.size() == 1) {
            features.get(indexes.get(0)).setTheOnlyNEColumn(true);
            return features.get(indexes.get(0)).getColId();
        }
        return -1;
    }

    //how many diverse values do we have in a column
    public void feature_valueDiversity(List<ColumnFeature> features, Table table) {
        for (ColumnFeature cf : features) {
            int col = cf.getColId();
            Set<String> uniqueValues_onRows = new HashSet<String>();
            Set<String> uniqueTokens_onRows = new HashSet<String>();
            int totalTokens = 0;
            for (int r = 0; r < table.getNumRows(); r++) {
                LTableContentCell c = table.getContentCell(r, col);
                uniqueValues_onRows.add(c.getText());

                for (String tok : c.getText().split("\\s+")) {
                    uniqueTokens_onRows.add(tok.trim());
                    totalTokens++;
                }
            }
            double diversity_1 = (double) uniqueValues_onRows.size() / table.getNumRows();
            double diversity_2 = (double) uniqueTokens_onRows.size() / table.getNumRows() / ((double) totalTokens / table.getNumRows());
            cf.setCellValueDiversity(diversity_1);
            cf.setTokenValueDiversity(diversity_2);
        }
    }

    //how does each header score against the table contexts
    public void feature_contextMatchScore(List<ColumnFeature> features, Table table) {
        int[] cols = new int[features.size()];

        for (int c = 0; c < features.size(); c++) {
            int col = features.get(c).getColId();
            cols[c] = col;
        }

        Map<Integer, Double> scores = ctxMatcher.match(table, cols);
        for (ColumnFeature cf : features) {
            Double s = scores.get(cf.getColId());
            s = s == null ? 0 : s;
            cf.setContextMatchScore(s);
        }


    }

    //how does each cell on each row score against a websearch result?
    public DoubleMatrix2D feature_webSearchScore(List<ColumnFeature> features, Table table) throws APIKeysDepletedException, IOException {
        DoubleMatrix2D scores = new SparseDoubleMatrix2D(table.getNumRows(), table.getNumCols());
        List<Integer> searchableCols = new ArrayList<Integer>();//which columns contain values that are searchable? (numbers ignored, for example)
        for (int i = 0; i < features.size(); i++) {
            DataTypeClassifier.DataType type = features.get(i).getMostDataType().getCandidateType();
            if (type.equals(DataTypeClassifier.DataType.NAMED_ENTITY) || type.equals(DataTypeClassifier.DataType.SHORT_TEXT)) {
                searchableCols.add(features.get(i).getColId());
            }
        }

        //then search row-by-row
        for (int r = 0; r < table.getNumRows(); r++) {
            //prepare search string
            String[] values_on_the_row = new String[searchableCols.size()];
            for (int c = 0; c < searchableCols.size(); c++) {
                int colId = searchableCols.get(c);
                LTableContentCell cell = table.getContentCell(r, colId);
                values_on_the_row[c] = websearchMatcher.normalizeString(cell.getText());
            }

            //perform search and compute matching scores
            Map<String, Double> scores_on_the_row = websearchMatcher.score(values_on_the_row);

            // compute search results against each column in searchableCols
            for (int index = 0; index < searchableCols.size(); index++) {
                int colId = searchableCols.get(index);
                String normalizedCellContent = values_on_the_row[index];
                if (normalizedCellContent.length() < 1)
                    continue;

                Double search_score = scores_on_the_row.get(normalizedCellContent);
                search_score = search_score == null ? 0 : search_score;
                scores.set(r, colId, search_score);
            }
        }

        return scores;
    }


    //since using web search is expensive, we can use data sampling technique to incrementally interpret main column
    public DoubleMatrix2D feature_webSearchScore_with_sampling(List<ColumnFeature> features, Table table,
                                                               TContentRowRanker sampleSelector,
                                                               StoppingCriteria stopper,
                                                               int minimumRows) throws APIKeysDepletedException, IOException {

        if (minimumRows > table.getNumRows())
            return feature_webSearchScore(features, table);

        DoubleMatrix2D scores = new SparseDoubleMatrix2D(table.getNumRows(), table.getNumCols());
        Map<Object, Double> state = new HashMap<Object, Double>();

        List<Integer> searchableCols = new ArrayList<Integer>();//which columns contain values that are searchable? (numbers ignored, for example)
        for (int i = 0; i < features.size(); i++) {
            DataTypeClassifier.DataType type = features.get(i).getMostDataType().getCandidateType();
            if (type.equals(DataTypeClassifier.DataType.NAMED_ENTITY) || type.equals(DataTypeClassifier.DataType.SHORT_TEXT)) {
                searchableCols.add(features.get(i).getColId());
            }
        }


        int[] rowRanking = sampleSelector.select(table);
        int rows = 0;
        //then search row-by-row
        for (int r : rowRanking) {
            //prepare search string
            String[] values_in_the_cell = new String[searchableCols.size()];
            for (int c = 0; c < searchableCols.size(); c++) {
                int colId = searchableCols.get(c);
                LTableContentCell cell = table.getContentCell(r, colId);
                values_in_the_cell[c] = websearchMatcher.normalizeString(cell.getText());
            }

            //perform search and compute matching scores
            Map<String, Double> scores_on_the_row = websearchMatcher.score(values_in_the_cell);

            // compute search results against each column in searchableCols
            for (int index = 0; index < searchableCols.size(); index++) {
                int colId = searchableCols.get(index);
                String normalizedCellContent = values_in_the_cell[index];
                if (normalizedCellContent.length() < 1)
                    continue;

                Double search_score = scores_on_the_row.get(normalizedCellContent);
                search_score = search_score == null ? 0 : search_score;
                scores.set(r, colId, search_score);

                //also update State
                Double score_in_the_state = state.get(colId);
                score_in_the_state = score_in_the_state == null ? 0 : score_in_the_state;
                score_in_the_state = score_in_the_state + search_score;
                state.put(colId, score_in_the_state);
            }

            rows++;
            if (/*rows>=minimumRows && */stopper.stop(state, table.getNumRows()))
                break;
        }

        return scores;
    }

    //check the syntactic feature (POS) of the header title. invalid POS include: prep (in theory only noun is valid. but
    //that may over-eliminate true pos
    public void feature_headerInvalidSyntactic(List<ColumnFeature> allNEColumnCandidates, Table table) {
        for (ColumnFeature cf : allNEColumnCandidates) {
            int col = cf.getColId();
            String headerText = table.getColumnHeader(col).getHeaderText();

            String[] tags = nlpTools.getPosTagger().tag(headerText.toLowerCase().split("\\s+"));
            if (tags[tags.length - 1].equals("IN") || tags[tags.length - 1].equals("TO"))
                cf.setInvalidPOS(true);
        }

    }

    public void feature_isColumnAcronymOrCode(List<ColumnFeature> allNEColumnCandidates, Table table) {
        for (ColumnFeature cf : allNEColumnCandidates) {
            int col = cf.getColId();
            int countAcronym_or_Code = 0;
            for (int r = 0; r < table.getNumRows(); r++) {
                String cellContent = table.getContentCell(r, col).getText().replaceAll("\\s+", " ").trim();
                if (cellContent.length() == 0)
                    continue;
                if (cellContent.length() < 15) {
                    int countWhiteSpace = 0, countNonWhiteSpace = 0, countLetters = 0, countDigits = 0;
                    boolean letters_are_all_cap = true;
                    for (int index = 0; index < cellContent.length(); index++) {
                        char c = cellContent.charAt(index);
                        if (Character.isWhitespace(c))
                            countWhiteSpace++;
                        else {
                            countNonWhiteSpace++;
                            if (Character.isLetter(c)) {
                                countLetters++;
                                if (!Character.isUpperCase(c))
                                    letters_are_all_cap = false;
                            } else if (Character.isDigit(c)) {
                                countDigits++;
                            }
                        }
                    }

                    int countSymbols = countNonWhiteSpace - countLetters - countDigits;
                    if (countWhiteSpace == 0 && countDigits > 0 && countLetters > 0) { //no white space, mixture of letters (whatever case) and digits
                        countAcronym_or_Code++;
                        // System.out.println(cellContent+" \tno white space, mixture of letters and digits");
                    } else if (countWhiteSpace == 0 && countLetters > 0 && letters_are_all_cap && cellContent.length() < 6) { //no white space, all uppercase letters (total < 6)
                        countAcronym_or_Code++;
                        //  System.out.println(cellContent+" \tno white space, all uppercase letters (total < 6)");
                    } else if (countWhiteSpace == 1 && letters_are_all_cap) { //1 white space, letters must all be uppercase
                        countAcronym_or_Code++;
                        //  System.out.println(cellContent+" \t1 white space, letters must all be uppercase");
                    }
                }


            }
            if (countAcronym_or_Code > (table.getNumRows() - cf.getEmptyCells()-countAcronym_or_Code))
                cf.setCode_or_Acronym(true);
        }

    }


}

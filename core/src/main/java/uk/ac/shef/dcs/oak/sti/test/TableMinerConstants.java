package uk.ac.shef.dcs.oak.sti.test;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class TableMinerConstants {
    public static int CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD = 0; //0 - best candidate contribute; 1-all candidates contribute. check the two methods taht uses this param
    public static int MAX_REFERENCE_ENTITY_FOR_DISAMBIGUATION = 0;
    public static boolean RELATED_COLUMN_HEADER_TYPING_ONLY_FROM_MAIN_COL_RELATIONS = false;
    public static boolean ALLOW_NEW_HEADERS_AT_DISAMBIGUATION_UPDATE = true;
    public static boolean RELATION_ALSO_CONTRIBUTES_TO_COLUMN_HEADER_SCORE = false;
    public static boolean USE_NESTED_RELATION_AND_FACTS_FOR_ENTITY_FEATURE = true;
    public static boolean USE_NESTED_RELATION_FOR_RELATION_INTERPRETATION = true;

    public static boolean ENFORCE_ONPSPD = false;
    public static boolean ENFORCE_OSPD = true;

    public static boolean DISCARD_SINGLE_CHAR_IN_BOW = true;

    public static boolean COMMIT_SOLR_PER_FILE = false;

    public static boolean BEST_CANDIDATE_CONTRIBUTE_COUNT_ONLY_ONCE = true;

    public static boolean FORCE_TOPICAPI_QUERY = false;
    public static boolean FORCE_SEARCHAPI_QUERY = false;
    public static final boolean INCLUDE_URL_IN_CLASS_BOW = true;

    public static List<String> stopwords_small = new ArrayList<String>();
    public static final boolean REVISE_HBR_BY_DC = true;

    public static boolean MAIN_COL_DETECT_USE_WEBSEARCH = true;
    public static final boolean IGNORE_NOTABLE_EXTRACTED_TYPE = false;

    public static final String SAMPLE_SIZE="300";
    public static final int UPDATE_PHASE_MAX_ITERATIONS=10;

    static {
        stopwords_small.add("of");
        stopwords_small.add("in");
        stopwords_small.add("the");
        stopwords_small.add("from");
        stopwords_small.add("to");
        stopwords_small.add("at");
        stopwords_small.add("on");
        stopwords_small.add("for");
        stopwords_small.add("by");
        stopwords_small.add("till");
        stopwords_small.add("until");
        stopwords_small.add("off");
        stopwords_small.add("onto");
        stopwords_small.add("into");
        stopwords_small.add("under");
        stopwords_small.add("below");
        stopwords_small.add("over");
        stopwords_small.add("above");
        stopwords_small.add("across");
        stopwords_small.add("through");

    }


}

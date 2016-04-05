package uk.ac.shef.dcs.sti;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class STIConstantProperty {
    public static final int TCELLDISAMBIGUATOR_MAX_REFERENCE_ENTITIES = 0;
    public static boolean RELATED_COLUMN_HEADER_TYPING_ONLY_FROM_MAIN_COL_RELATIONS = false;
    public static boolean RELATION_ALSO_CONTRIBUTES_TO_COLUMN_HEADER_SCORE = false;
    public static final boolean COMMIT_SOLR_PER_FILE = false;

    public static boolean USE_NESTED_RELATION_FOR_RELATION_INTERPRETATION = true;

    public static final boolean ENFORCE_OSPD = true;
    public static final boolean ENTITYBOW_INCLUDE_INDIRECT_ATTRIBUTE = true;
    public static final boolean BOW_DISCARD_SINGLE_CHAR = true; //whether discard single char words from BoW
    public static final boolean CLAZZBOW_INCLUDE_URI = true;
    public static final List<String> FUNCTIONAL_STOPWORDS = new ArrayList<>();
    public static final boolean REVISE_RELATION_ANNOTATION_BY_DC = true;
    public static final boolean SUBJECT_COLUMN_DETECTION_USE_WEBSEARCH = true;
    public static final String SAMPLE_SIZE="300";
    public static final int UPDATE_PHASE_MAX_ITERATIONS=10;

    static {
        FUNCTIONAL_STOPWORDS.add("of");
        FUNCTIONAL_STOPWORDS.add("in");
        FUNCTIONAL_STOPWORDS.add("the");
        FUNCTIONAL_STOPWORDS.add("from");
        FUNCTIONAL_STOPWORDS.add("to");
        FUNCTIONAL_STOPWORDS.add("at");
        FUNCTIONAL_STOPWORDS.add("on");
        FUNCTIONAL_STOPWORDS.add("for");
        FUNCTIONAL_STOPWORDS.add("by");
        FUNCTIONAL_STOPWORDS.add("till");
        FUNCTIONAL_STOPWORDS.add("until");
        FUNCTIONAL_STOPWORDS.add("off");
        FUNCTIONAL_STOPWORDS.add("onto");
        FUNCTIONAL_STOPWORDS.add("into");
        FUNCTIONAL_STOPWORDS.add("under");
        FUNCTIONAL_STOPWORDS.add("below");
        FUNCTIONAL_STOPWORDS.add("over");
        FUNCTIONAL_STOPWORDS.add("above");
        FUNCTIONAL_STOPWORDS.add("across");
        FUNCTIONAL_STOPWORDS.add("through");

    }


}

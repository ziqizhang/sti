package uk.ac.shef.dcs.oak.lodie;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 05/10/12
 * Time: 15:52
 */
public enum PlaceHolder {

    TABLE_HEADER_UNKNOWN("H_Unknown"),
    LIST_ITEM_SEPARATOR("|"),
    TRIPLE_LITERAL_VALUE_QUOTE("'");

    private String value;

    private PlaceHolder(String value){
        this.value=value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

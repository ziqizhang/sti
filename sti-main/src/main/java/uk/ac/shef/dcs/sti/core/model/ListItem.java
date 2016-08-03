package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 10/10/12
 * Time: 12:25
 */
public class ListItem implements Serializable {
    private static final long serialVersionUID = -8136725546789405913L;
    private String text;
    private Map<String, String> valueURIs;

    public ListItem(String text) {
        valueURIs = new LinkedHashMap<>();
        this.text = text;
    }

    public boolean equals(Object o) {
        if (o instanceof ListItem) {
            ListItem c = (ListItem) o;
            return c.getText().equals(getText());
        }
        return false;
    }

    public int hashCode() {
        return getText().hashCode();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, String> getValuesAndURIs() {
        return valueURIs;
    }

    public String toString(){
        return text;
    }

}

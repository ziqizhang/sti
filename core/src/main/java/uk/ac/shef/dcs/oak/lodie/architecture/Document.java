package uk.ac.shef.dcs.oak.lodie.architecture;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 22/10/12
 * Time: 16:18
 *
 * Represents a document containing annotations
 */
public class Document<K> {
    private URL docURL;
    private List<K> content;

    public Document(URL url){
        this.docURL=url;
        content = new ArrayList<K>();
    }

    public URL getDocURL() {
        return docURL;
    }

    public void setDocURL(URL docURL) {
        this.docURL = docURL;
    }


    public List<K> getContent() {
        return content;
    }

    public void setContent(List<K> content) {
        this.content = content;
    }
}

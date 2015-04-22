package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder;

import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 01/03/13
 * Time: 15:55
 */
public abstract class CandidateFinder {
    protected QueryCache cache;

    public CandidateFinder(QueryCache cache) {
        this.cache = cache;
    }

    /**
     * This method should implement the strategies for caching, if any
     *
     * @param text
     * @return
     */
    protected abstract List findCandidatesInKB(String text);

    protected abstract List findCandidatesInCache(String text);

    public List findCandidates(String keyword) throws IOException, SolrServerException {
        List<String[]> rs = findCandidatesInCache(keyword);
        if (rs == null) {
            rs = findCandidatesInKB(keyword);
        }
        return rs;
    }

    protected List<String> getVariants(String keyword) {
            List<String> ng = new ArrayList<String>();

            ng.add(keyword);
            ng.add(keyword.toLowerCase());

            return ng;
        }

    protected List<String> getNGram(String keyword) {
        String[] tokens = keyword.split("\\s+");
        List<String> ng = new ArrayList<String>();

        for (int start = 0; start < tokens.length; start++) {
            for (int end = start + 1; end < tokens.length; end++) {
                StringBuilder sb = new StringBuilder();
                for (int i = start; i <= end; i++) {
                    sb.append(tokens[i]).append(" ");
                }
                ng.add(sb.toString().trim());
            }
        }
        for (String tk : tokens)
            ng.add(tk.trim());

        Collections.sort(ng, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int length1 = o1.split("\\s+").length;
                int length2 = o2.split("\\s+").length;
                if (length1 > length2)
                    return -1;
                if (length1 < length2)
                    return 1;
                return 0;
            }
        });

        return ng;
    }
}

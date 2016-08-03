package uk.ac.shef.dcs.sti.parser.list;

import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.core.model.List;
import uk.ac.shef.dcs.sti.parser.list.splitter.ListItemSplitter;
import uk.ac.shef.dcs.sti.parser.list.validator.ListValidator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 10/10/12
 * Time: 12:57
 */
public class ListXtractorHTML extends ListXtractor {
    private String[] listTagSelectors;


    public ListXtractorHTML(ListItemSplitter tokenizer, ListValidator... validator) {
        super(tokenizer, validator);
        listTagSelectors = new String[]{"UL","OL"};
    }

    @SuppressWarnings("unchecked")
    @Override
    public java.util.List extract(String input, String sourceId) throws IOException {
        /*if (sourceId.startsWith("Altruism"))
            System.out.println();*/
        java.util.List rs = new ArrayList<List>();

        parser = new TagSoupParser(new ByteArrayInputStream(input.getBytes()), sourceId);
        Document doc = null;
        try {
            doc = parser.getDOM();
        } catch (IOException e) {
            return rs;
        }

        int listCount = 0;
        for (String selectTag : listTagSelectors) {
            java.util.List<Node> lists=DomUtils.findAllByTag(doc, selectTag);

            for(Node n: lists){
                listCount++;

                if (!isValidPosition(n))
                    continue;
                //todo: extract context for list
                String[] contexts = new String[0];

                List list = extractList(n, String.valueOf(listCount),
                        sourceId, contexts);
                if (list != null)
                    rs.add(list);
            }

        }
        return rs;
    }

    protected boolean isValidPosition(Node ulElement) {
        Node par = ulElement.getParentNode();
        if (par != null && !par.getNodeName().equalsIgnoreCase("body"))
            return false;
        return true;
    }
}

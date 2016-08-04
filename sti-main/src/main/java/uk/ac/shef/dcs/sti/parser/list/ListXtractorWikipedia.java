package uk.ac.shef.dcs.sti.parser.list;

import info.bliki.wiki.model.WikiModel;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.parser.list.splitter.ListItemSplitter;
import uk.ac.shef.dcs.sti.parser.list.validator.ListValidator;

import java.io.IOException;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 09/10/12
 * Time: 11:19
 * <p/>
 * selects
 */
public class ListXtractorWikipedia extends ListXtractorHTML {
    private WikiModel model;

    private final String[] STOP_HEADINGS = {"see also",
            "external links", "further readings", "other readings"}; //a list of headings that if found for a list, the list should be discarded

    public ListXtractorWikipedia(ListItemSplitter tokenizer, ListValidator... validators) {
        super(tokenizer, validators);
        model = new WikiModel("/${image}", "/${title}");
    }

    @Override
    public java.util.List extract(String input, String sourceId) throws IOException {
        String html = model.render(input);
        return super.extract(html, sourceId);
    }

    @Override
    protected boolean isValidPosition(Node ulElement) {
        Node par = ulElement.getParentNode();
        if (par != null && !par.getNodeName().equalsIgnoreCase("body"))
            return false;

        Node previousSibling = ulElement.getPreviousSibling();
        while (previousSibling != null) {
            if (previousSibling.getNodeName().toLowerCase().startsWith("h")) {
                String headerText = previousSibling.getTextContent();
                for(String stopHeading: STOP_HEADINGS){
                    if(headerText.equalsIgnoreCase(stopHeading))
                        return false;
                }
                return true;
            } else
                previousSibling = previousSibling.getPreviousSibling();
        }

        return true;
    }

}

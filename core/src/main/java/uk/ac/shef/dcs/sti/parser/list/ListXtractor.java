package uk.ac.shef.dcs.sti.parser.list;

import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.shef.dcs.sti.core.model.List;
import uk.ac.shef.dcs.sti.core.model.ListItem;
import uk.ac.shef.dcs.sti.parser.list.splitter.ListItemSplitter;
import uk.ac.shef.dcs.sti.parser.list.validator.ListValidator;

import java.io.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 09/10/12
 * Time: 11:06
 */
public abstract class ListXtractor {

    private ListItemSplitter tokenizer;
    protected ListValidator[] validators;
    protected TagSoupParser parser;

    public ListXtractor(ListItemSplitter tokenizer, ListValidator... validators) {
        this.validators = validators;
        this.tokenizer = tokenizer;
    }

    public abstract java.util.List extract(String input, String sourceId) throws IOException;

    /**
     * ONLY extracts IMMEDIATE children
     *
     * @param listElement
     * @param listId
     * @param sourceId
     * @param contexts
     * @return
     */
    protected List extractList(Node listElement, String listId, String sourceId, String... contexts) {
        /*if (sourceId.startsWith("Anarchism"))
            System.out.print("");*/
        List list = new List(sourceId, listId);
        for (String ctx : contexts)
            list.addContext(ctx);

        NodeList it = listElement.getChildNodes();
        for (int i = 0; i < it.getLength(); i++) {
            Node liElement = it.item(i);
            if(liElement.getNodeType()==3)
                continue;
            ListItem li = tokenizer.tokenize(liElement);
            if (li != null)
                list.addItem(li);
        }

        for (ListValidator v : validators) {
            if (!v.isValid(list))
                return null;
        }
        return list;
    }

    public static void serialize(List list, String targetDir) throws IOException {
        File dir = new File(targetDir);
        if (!dir.exists())
            dir.mkdirs();
        String filename = targetDir + File.separator + list.getSourceId().replaceAll("[^\\d\\w]", "_") + "_" + list.getListId();

        FileOutputStream fileOut =
                new FileOutputStream(filename);
        ObjectOutputStream out =
                new ObjectOutputStream(fileOut);
        out.writeObject(list);
        out.close();
        fileOut.close();
    }

    public static List deserialize(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fileIn =
                new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        List list = (List) in.readObject();
        in.close();
        fileIn.close();
        return list;
    }
}

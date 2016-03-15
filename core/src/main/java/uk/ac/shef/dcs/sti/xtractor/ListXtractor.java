package uk.ac.shef.dcs.sti.xtractor;

import org.apache.any23.extractor.html.TagSoupParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.shef.dcs.sti.rep.LList;
import uk.ac.shef.dcs.sti.rep.LListItem;

import java.io.*;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 09/10/12
 * Time: 11:06
 */
public abstract class ListXtractor {

    private ListElementTokenizer tokenizer;
    protected ListValidator[] validators;
    protected TagSoupParser parser;

    public ListXtractor(ListElementTokenizer tokenizer, ListValidator... validators) {
        this.validators = validators;
        this.tokenizer = tokenizer;
    }

    public abstract List<LList> extract(String input, String sourceId);

    /**
     * ONLY extracts IMMEDIATE children
     *
     * @param listElement
     * @param listId
     * @param sourceId
     * @param contexts
     * @return
     */
    protected LList extractList(Node listElement, String listId, String sourceId, String... contexts) {
        /*if (sourceId.startsWith("Anarchism"))
            System.out.print("");*/
        LList list = new LList(sourceId, listId);
        for (String ctx : contexts)
            list.addContext(ctx);

        NodeList it = listElement.getChildNodes();
        for (int i = 0; i < it.getLength(); i++) {
            Node liElement = it.item(i);
            if(liElement.getNodeType()==3)
                continue;
            LListItem li = tokenizer.tokenize(liElement);
            if (li != null)
                list.addItem(li);
        }

        for (ListValidator v : validators) {
            if (!v.isValid(list))
                return null;
        }
        return list;
    }

    public static void serialize(LList list, String targetDir) throws IOException {
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

    public static LList deserialize(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fileIn =
                new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        LList list = (LList) in.readObject();
        in.close();
        fileIn.close();
        return list;
    }
}

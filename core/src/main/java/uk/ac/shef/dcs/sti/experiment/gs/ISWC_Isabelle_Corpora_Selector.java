package uk.ac.shef.dcs.sti.experiment.gs;

import info.aduna.io.FileUtil;
import uk.ac.shef.dcs.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 31/03/14
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */
public class ISWC_Isabelle_Corpora_Selector {

    public static void main(String[] args) throws IOException {
        String index_file = args[0];
        String infolder = args[1];
        String outfolder = args[2];
        String index_filter_file = args[3];

        List<String> keep_websites = FileUtils.readList(index_filter_file, false);
        Iterator<String> it = keep_websites.iterator();
        while (it.hasNext()) {
            String l = it.next();
            if (l.startsWith("#"))
                it.remove();
        }

        //"http://www.reverbnation.com";
        List<String> indexes = FileUtils.readList(index_file, false);
        Set<String> keep_file_names = new HashSet<String>();
        for (String l : indexes) {
            String[] parts = l.split("\t");
            if (parts.length < 2)
                continue;
            boolean keep = false;
            for (String kw : keep_websites) {
                if (parts[1].trim().startsWith(kw)) {
                    keep = true;
                    break;
                }
            }
            if (keep)
                keep_file_names.add(parts[0].trim());
        }

        //then copy
        for (String fname : keep_file_names) {
            if(new File(infolder + "/" + fname).exists())
                FileUtil.copyFile(new File(infolder + "/" + fname), new File(outfolder + "/" + fname));
        }
    }
}

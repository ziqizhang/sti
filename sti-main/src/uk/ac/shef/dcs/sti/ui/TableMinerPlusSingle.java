package uk.ac.shef.dcs.sti.ui;

import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.experiment.TableMinerPlusBatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class TableMinerPlusSingle extends TableMinerPlusBatch {


    public TableMinerPlusSingle(String propertyFile) throws IOException, STIException {
        super(propertyFile);
    }

    public void process(String userId,
                        String email,
                        String inFileURL,
                        String outFolderStr,
                        String tableParserClass,
                        String tableIndexes,
                        String configJSONFile,
                        String host) throws STIException, IOException {
        File outFolder = new File(outFolderStr+ File.separator+userId);
        if(!outFolder.exists())
            outFolder.mkdirs();

        InputFilePreview downloader = new InputFilePreview();
        downloader.downloadWebpage(inFileURL, outFolder.getAbsolutePath());
        Set<Integer> selectedTableIndexes = getTableIndexes(tableIndexes);
        try {
            String sourceTableFile = findDownloadedFile(outFolder.getAbsolutePath());
            if(sourceTableFile==null){
                throw new STIException("No file is downloaded for input url:"+inFileURL);
            }
            if (sourceTableFile.startsWith("\"") && sourceTableFile.endsWith("\""))
                sourceTableFile = sourceTableFile.substring(1, sourceTableFile.length() - 1).trim();
            //System.out.println(count + "_" + sourceTableFile + " " + new Date());
            List<Table> tables = loadTable(sourceTableFile, getTableParser(tableParserClass));

            int count=-1;
            for (Table table : tables) {
                count++;
                if(!selectedTableIndexes.contains(count)){
                    continue;
                }
                boolean complete = process(
                        table,
                        sourceTableFile,
                        getTAnnotationWriter(), outFolder.toString(),
                        Boolean.valueOf(properties.getProperty(PROPERTY_PERFORM_RELATION_LEARNING)));

                if (STIConstantProperty.SOLR_COMMIT_PER_FILE)
                    commitAll();
                if (!complete) {
                    recordFailure(count, sourceTableFile, sourceTableFile);
                }
            }

            String visitPage = generateReturnWebpage(outFolder,
                    host+outFolderStr+ File.separator+userId);

            String emailMsg = "Here's your output file(s), finally I can take a break, phew...\n" +
                    visitPage + "\n\n- TableMiner+";
            EmailHandler.sendCompletionEmail(configJSONFile, email, emailMsg);

        } catch (Exception e) {
            e.printStackTrace();
            recordFailure(0, inFileURL, inFileURL);
        }


        closeAll();

        System.exit(0);
    }

    private String generateReturnWebpage(File outFolder, String url) throws FileNotFoundException {
        File indexFile = new File(outFolder+File.separator+"index.htm");
        PrintWriter p =new PrintWriter(indexFile);

        StringBuilder sb = new StringBuilder("<!DOCTYPE html>\n<HTML dir=\"ltr\" lang=\"en\">\n<head/>\n<body>\n");
        sb.append("<h1>Click a link below to visualize annotation results</h1>\n");
        sb.append("<table border=\"1\">\n  <tr>\n").append("    <th>Link</th>\n").
                append("    <th>Completed at</th>\n").
                append("  </tr>\n");

        for(File f: outFolder.listFiles()){
            if(f.getName().endsWith("html")){
                sb.append("  <tr>\n").
                        append("    <td><a href=\"").append(f.getName()).append("\">").append(f.getName()).
                        append("</a></td>\n").
                        append("    <td>").append(new Date(f.lastModified()).toString()).append("</td>").
                        append("  </tr>");

            }
        }

        sb.append("</table>\n").append("</body>\n</html>");

        p.println(sb.toString());
        p.close();
        if(url.endsWith("/"))
            return url+"index.html";
        return url+File.separator+"index.htm";
    }

    private String findDownloadedFile(String absolutePath) {
        File f = new File(absolutePath);
        for(File sf: f.listFiles()){
            if(sf.toString().endsWith(".html")||
                    sf.toString().endsWith(".htm")){
                return sf.toString();
            }
        }
        return null;
    }

    private Set<Integer> getTableIndexes(String tableIndexes) {
        Set<Integer> indexes = new HashSet<>();
        for(String i: tableIndexes.split(",")){
            indexes.add(Integer.valueOf(i.trim()));
        }
        return indexes;
    }

    public static void main(String[] args) throws IOException, STIException {
        String userId=args[0];
        String userEmail=args[1];
        String inFileURL = args[2];
        String outFolderStr = args[3];

        String tableParserClass=args[4];
        String tableIndexes=args[5];
        TableMinerPlusSingle tmp = new TableMinerPlusSingle(args[6]);
        tmp.process(userId,
                userEmail,
                inFileURL,
                outFolderStr,
                tableParserClass,
                tableIndexes,
                args[7],
                args[8]);

    }

}

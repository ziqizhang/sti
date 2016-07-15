package uk.ac.shef.dcs.sti.ui;

import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.experiment.TableMinerPlusBatch;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by - on 15/07/2016.
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
                        String tableIndexes) throws STIException {
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

        } catch (Exception e) {
            e.printStackTrace();
            recordFailure(0, inFileURL, inFileURL);
        }


        closeAll();

        //todo: prepare webpage
        //todo: email notification

        System.exit(0);
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
                tableIndexes);

    }

}

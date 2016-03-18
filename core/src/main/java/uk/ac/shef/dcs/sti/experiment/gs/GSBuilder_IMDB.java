package uk.ac.shef.dcs.sti.experiment.gs;

import org.apache.any23.util.FileUtils;
import uk.ac.shef.dcs.sti.algorithm.tm.TripleGenerator;
import uk.ac.shef.dcs.sti.io.TAnnotationWriter;
import uk.ac.shef.dcs.sti.rep.CellAnnotation;
import uk.ac.shef.dcs.sti.rep.TContentCell;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.sti.rep.LTableAnnotation;
import uk.ac.shef.dcs.sti.xtractor.validator.TabValGeneric;
import uk.ac.shef.dcs.sti.xtractor.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.xtractor.TableNormalizerFrequentRowLength;
import uk.ac.shef.dcs.sti.xtractor.TableObjCreatorIMDB;
import uk.ac.shef.dcs.sti.xtractor.TableXtractorIMDB;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseEntity;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseQueryHelper;

import java.io.*;
import java.util.HashMap;
import java.util.List;

/**

 */
public class GSBuilder_IMDB {

    public static void main(String[] args) throws IOException {
        GSBuilder_IMDB gsBuilder = new GSBuilder_IMDB();
        //todo:this will not work
        FreebaseQueryHelper queryHelper = null; //new FreebaseQueryHelper(args[2]);
        TAnnotationWriter writer = new TAnnotationWriter(new TripleGenerator("http://www.freebase.com", "http://dcs.shef.ac.uk"));
        String inFolder = args[0];
        String outFolder = args[1];
        //read imdb page, create table object

        TableXtractorIMDB xtractor = new TableXtractorIMDB(new TableNormalizerFrequentRowLength(true),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorIMDB(),
                new TabValGeneric());
        int count = 0;
        File[] all = new File(inFolder).listFiles();
        System.out.println(all.length);
        for (File f : all) {

            count++;
            System.out.println(count);
            String inFile = f.toString();
            try {
                String fileContent = FileUtils.readFileContent(new File(inFile));
                List<Table> tables = xtractor.extract(fileContent, inFile);

                if (tables.size() == 0)
                    continue;

                Table table = tables.get(0);
                //gs annotator
                System.out.println(f+", with rows: "+table.getNumRows());
                LTableAnnotation annotations = gsBuilder.annotate(table, queryHelper);
                if (annotations != null) {
                    int count_annotations = 0;
                    for (int row = 0; row < table.getNumRows(); row++) {
                        for (int col = 0; col < table.getNumCols(); col++) {
                            CellAnnotation[] cas = annotations.getContentCellAnnotations(row, col);
                            if (cas != null && cas.length > 0)
                                count_annotations++;
                        }
                    }

                    if (count_annotations > 0) {
                        gsBuilder.save(table, annotations, outFolder, writer);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                PrintWriter missedWriter = null;
                try {
                    missedWriter = new PrintWriter(new FileWriter("missed.csv", true));
                } catch (IOException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                missedWriter.println(inFile);
                missedWriter.close();
            }

        }
    }

    public LTableAnnotation annotate(Table table, FreebaseQueryHelper queryHelper) throws IOException {
        LTableAnnotation tableAnnotation = new LTableAnnotation(table.getNumRows(), table.getNumCols());
        for (int row = 0; row < table.getNumRows(); row++) {
            TContentCell ltc = table.getContentCell(row, 0);
            String text = ltc.getText();
            int start = text.indexOf("/name/");
            if (start == -1)
                continue;
            else
                start = start + 6;
            int end = text.lastIndexOf("/");
            if (end == -1)
                continue;

            String imdb_id = text.substring(start, end).trim();
            List<FreebaseEntity> list = queryHelper.searchapi_topics_with_name_and_type(imdb_id, "any", false, 5);
            if (list == null || list.size() == 0)
                continue;
            CellAnnotation[] cas = new CellAnnotation[1];
            cas[0] = new CellAnnotation(text, list.get(0), 1.0, new HashMap<String, Double>());
            tableAnnotation.setContentCellAnnotations(row, 1, cas);
        }
        return tableAnnotation;
    }


    public void save(Table table, LTableAnnotation annotations, String outFolder, TAnnotationWriter writer) throws FileNotFoundException {
        String fileId = table.getSourceId();
        fileId = fileId.replaceAll("\\\\","/");
        int trim = fileId.lastIndexOf("/");
        if(trim!=-1)
            fileId=fileId.substring(trim+1).trim();
        writer.writeHTML(table, annotations, outFolder + File.separator + fileId);
        String annotation_keys = outFolder + File.separator + fileId + ".keys";
        PrintWriter p = new PrintWriter(annotation_keys);
        for (int row = 0; row < table.getNumRows(); row++) {
            for (int col = 0; col < table.getNumCols(); col++) {
                CellAnnotation[] anns = annotations.getContentCellAnnotations(row, col);
                if (anns != null && anns.length > 0) {
                    p.println(row + "," + col + "," + anns[0].getAnnotation().getId());
                }
            }
        }
        p.close();
    }

}

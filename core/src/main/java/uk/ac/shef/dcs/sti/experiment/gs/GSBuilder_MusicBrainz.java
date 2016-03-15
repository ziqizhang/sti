package uk.ac.shef.dcs.sti.experiment.gs;

import org.apache.any23.util.FileUtils;
import uk.ac.shef.dcs.sti.algorithm.tm.TripleGenerator;
import uk.ac.shef.dcs.sti.io.LTableAnnotationWriter;
import uk.ac.shef.dcs.sti.rep.CellAnnotation;
import uk.ac.shef.dcs.sti.rep.LTable;
import uk.ac.shef.dcs.sti.rep.LTableAnnotation;
import uk.ac.shef.dcs.sti.rep.LTableContentCell;
import uk.ac.shef.dcs.sti.xtractor.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.xtractor.TableNormalizerDummy;
import uk.ac.shef.dcs.sti.xtractor.TableObjCreatorMusicBrainz;
import uk.ac.shef.dcs.sti.xtractor.TableXtractorMusicBrainz;
import uk.ac.shef.dcs.sti.xtractor.validator.TabValGeneric;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseEntity;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseQueryHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class GSBuilder_MusicBrainz {

    public static void main(String[] args) throws IOException {
        GSBuilder_MusicBrainz gsBuilder = new GSBuilder_MusicBrainz();
        FreebaseQueryHelper queryHelper = new FreebaseQueryHelper(args[2]);
        LTableAnnotationWriter writer = new LTableAnnotationWriter(new TripleGenerator("http://www.musicbrainz.org", "http://dcs.shef.ac.uk"));
        String inFolder = args[0];
        String outFolder = args[1];
        //read imdb page, create table object

        TableXtractorMusicBrainz xtractor = new TableXtractorMusicBrainz(new TableNormalizerDummy(),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorMusicBrainz(),
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
                List<LTable> tables = xtractor.extract(fileContent, inFile);

                if (tables.size() == 0)
                    continue;

                LTable table = tables.get(0);
                //gs annotator
                System.out.println(f + ", with rows: " + table.getNumRows());
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

    public LTableAnnotation annotate(LTable table, FreebaseQueryHelper queryHelper) throws IOException {
        Map<String, List<FreebaseEntity>> cache_for_table = new HashMap<String, List<FreebaseEntity>>();

        LTableAnnotation tableAnnotation = new LTableAnnotation(table.getNumRows(), table.getNumCols());
        for (int row = 0; row < table.getNumRows(); row++) {
            for (int col = 0; col < table.getNumCols(); col++) {
                /* if(col==1)
                System.out.println();*/
                LTableContentCell ltc = table.getContentCell(row, col);
                String text = ltc.getText();
                String url = ltc.getOther_text();

                int start = -1, end = -1;
                if (url != null) {
                    start = url.lastIndexOf("/");
                    if (start == -1)
                        continue;
                    else
                        start = start + 1;
                    end = url.length();
                    if (end == -1)
                        continue;
                }

                if (start > -1 && end > -1) {
                    String music_brainz_id = "";
                    try {
                        music_brainz_id = url.substring(start, end).trim();
                    } catch (StringIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        System.out.println();
                    }

                    List<FreebaseEntity> list = cache_for_table.get(music_brainz_id);
                    if (list == null) {
                        list = queryHelper.searchapi_topics_with_name_and_type(music_brainz_id, "any", false, 5);
                        if (list == null)
                            list = new ArrayList<FreebaseEntity>();
                        cache_for_table.put(music_brainz_id, list);
                    }
                    if (list.size() == 0)
                        continue;
                    CellAnnotation[] cas = new CellAnnotation[1];
                    cas[0] = new CellAnnotation(text, list.get(0), 1.0, new HashMap<String, Double>());
                    tableAnnotation.setContentCellAnnotations(row, col, cas);
                }
            }
        }
        return tableAnnotation;
    }


    public void save(LTable table, LTableAnnotation annotations, String outFolder, LTableAnnotationWriter writer) throws FileNotFoundException {
        String fileId = table.getSourceId();
        fileId = fileId.replaceAll("\\\\", "/");
        int trim = fileId.lastIndexOf("/");
        if (trim != -1)
            fileId = fileId.substring(trim + 1).trim();
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

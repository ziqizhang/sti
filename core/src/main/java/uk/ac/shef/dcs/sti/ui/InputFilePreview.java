package uk.ac.shef.dcs.sti.ui;


import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.parser.table.Browsable;

import java.io.*;
import java.net.URL;
import java.util.List;

/**
 * Created by - on 11/07/2016.
 */
public class InputFilePreview {

    private static final String DOWNLOAD_WEBPAGE_SUFFIX = ".download.html";
    private static final Logger log = Logger.getLogger(InputFilePreview.class.getName());

    protected void cleanDirectory(String folder) throws IOException {
        File f = new File(folder);
        if (!f.exists())
            f.mkdirs();
        else
            FileUtils.cleanDirectory(new File(folder));
    }

    public String downloadWebpage(String urlStr, String folder) throws STIException {
        String line;
        try {
            cleanDirectory(folder);
            URL url = new URL(urlStr);
            InputStream is = url.openStream();  // throws an IOException
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String targetFile=folder + File.separator + url.getFile().replaceAll("[^a-zA-Z0-9]", "_")
                    +DOWNLOAD_WEBPAGE_SUFFIX;
            PrintWriter p = new PrintWriter(targetFile);

            while ((line = br.readLine()) != null) {
                p.println(line);
            }
            return targetFile;
        } catch (Exception e) {
            throw new STIException(e);
        }

       /* final WebClient webClient = new WebClient();
        try  {
            final HtmlPage page = webClient.getPage("https://en.wikipedia.org/wiki/Commedia_all%27italiana");

            page.save(new File("/Users/-/work/sti/output/saved.html"));
            System.out.println();
        }catch (Exception e){
            e.printStackTrace();
        }*/
    }


    public static void main(String[] args) throws STIException, ClassNotFoundException, IllegalAccessException, InstantiationException, FileNotFoundException {
        InputFilePreview ifp = new InputFilePreview();
        log.info("Downloading user requested webpage:" + args[0] + ", into:" + args[1]);
        String downloaded=ifp.downloadWebpage(/*"https://en.wikipedia.org/wiki/Commedia_all%27italiana",
                "/Users/-/work/sti/output/saved"*/
                args[0], args[1]);

        log.info("Instantiating table parser class:"+args[2]);
        String tableParserClass = args[2];
        Browsable parser = (Browsable) Class.forName(tableParserClass).newInstance();

        log.info("Parsing table input file and saving into:"+args[1]);
        List<String> xpaths= parser.extract(downloaded,
                args[0].replaceAll("[^a-zA-Z0-9]", "_"),
                args[1]);
        Gson gson = new Gson();
        String s=gson.toJson(xpaths);
        PrintWriter p = new PrintWriter(args[1]+File.separator+"xpaths.json");
        p.print(s);
        p.close();

    }
}

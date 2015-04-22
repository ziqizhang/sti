package uk.ac.shef.dcs.oak.lodie.test;

import uk.ac.shef.dcs.oak.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 19/03/14
 * Time: 12:15
 * To change this template use File | Settings | File Templates.
 */
public class TestDeleteFilesUtil {
    public static void main(String[] args) throws IOException {
        String filesToDelete = args[0];
        String filesSourceFolder=args[1];

        List<String> delete = new ArrayList<String>();
        for(String l: FileUtils.readList(filesToDelete,false)){
            l = l.replaceAll("\\\\","/");
            int trim = l.lastIndexOf("/");
            trim=trim==-1?0:trim+1;
            l = l.substring(trim,l.length()).trim();
            if(l.length()>0)
                delete.add(l);
        }

        for(File f: new File(filesSourceFolder).listFiles()){
            for(String d: delete){
                if(d.equals(f.getName())||d.equals(f.getName()+".keys"))
                    f.delete();
            }
        }
    }
}

/*
package uk.ac.shef.dcs.oak.lodie.test;

import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexInput;

import java.io.File;
import java.io.FilenameFilter;

*/
/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 25/03/14
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 *//*

public class TestCorruptIndex {
    public static void main(String[] args) {
        String path = "";// path to index
                File file = new File(path);
        Directory directory = FSDirectory.getDirectory(file, false);

        String[] files = file.list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".cfs");
            }

        });

        SegmentInfos infos = new SegmentInfos();
        int counter = 0;
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i];
            String segmentName = fileName.substring(1, fileName.lastIndexOf('.'));

            int segmentInt = Integer.parseInt(segmentName,Character.MAX_RADIX);
            counter = Math.max(counter, segmentInt);

            segmentName = fileName.substring(0, fileName.lastIndexOf('.'));

            Directory fileReader = new CompoundFileReader(directory,fileName);
            IndexInput indexStream = fileReader.openInput(segmentName + ".fdx");
            int size = (int)(indexStream.length() / 8);
            indexStream.close();
            fileReader.close();

            SegmentInfo segmentInfo = new SegmentInfo(segmentName,size,directory);
            infos.addElement(segmentInfo);
        }

        infos.counter = counter++;

        infos.write(directory);

    }
}
*/

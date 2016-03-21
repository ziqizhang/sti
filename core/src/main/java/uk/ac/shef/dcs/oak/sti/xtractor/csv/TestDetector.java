/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.shef.dcs.oak.sti.xtractor.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.universalchardet.UniversalDetector;

public class TestDetector {
    public String detect(File f) {
        byte[] buf = new byte[4096];

        java.io.FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream(f);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TestDetector.class.getName()).log(Level.SEVERE, null, ex);
        }

        // (1)
        UniversalDetector detector = new UniversalDetector(null);

        // (2)
        int nread;
        try {
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
        } catch (IOException ex) {
            Logger.getLogger(TestDetector.class.getName()).log(Level.SEVERE, null, ex);
        }

        // (3)
        detector.dataEnd();

        // (4)
        String encoding = detector.getDetectedCharset();
        if (encoding != null) {
            System.out.println("Detected encoding = " + encoding);
        } else {
            System.out.println("No encoding detected.");
        }

        // (5)
        detector.reset();
        return encoding;
    }
}

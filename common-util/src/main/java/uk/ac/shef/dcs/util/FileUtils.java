package uk.ac.shef.dcs.util;

import java.io.*;
import java.util.*;

/**
 * Several utility methods related to files.
 *
 * @author <a href="mailto:z.zhang@dcs.shef.ac.uk">Ziqi Zhang</a>
 */
public class FileUtils {

    public static List<File> listFilesRecursive(List<File> files, File dir, String... extFilter)
    {
        if (files == null)
            files = new LinkedList<File>();

        if (!dir.isDirectory())
        {
            files.add(dir);
            return files;
        }

        for (File file : dir.listFiles())
            listFilesRecursive(files, file);
        return files;
    }

    /**
     * Read input raw text file as a list
     *
     * @param path      input file path
     * @param lowercase whether to convert input string to lowercase
     * @return
     * @throws IOException
     */
    public static List<String> readList(final String path, final boolean lowercase) throws IOException {
        List<String> res = new ArrayList<String>();
        final BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.equals("")) continue;
            if (lowercase) res.add(line.toLowerCase());
            else res.add(line);
        }

        reader.close();
        return res;
    }

    public static List<String> readList(final String path, final boolean lowercase, final String charset) throws IOException {
        List<String> res = new ArrayList<String>();
        final InputStreamReader ir = new InputStreamReader(new FileInputStream(path), charset);

        final BufferedReader reader = new BufferedReader(ir);
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.equals("")) continue;
            if (lowercase) res.add(line.toLowerCase());
            else res.add(line);
        }

        reader.close();
        return res;
    }


    /**
     * <p>Adds a separator character to the end of the filename if it does not have one already.</p>
     *
     * @param filename the filename.
     * @return the filename with a separator at the end.
     */
    public static String addSeparator(final String filename) {
        if (filename != null && !filename.endsWith(File.separator)) return filename + File.separator;

        return filename;
    }


    public static String[] splitCSV(String line) throws IOException {
        java.io.StringReader r = new java.io.StringReader(line);
        return splitCSV(new BufferedReader(r));
    }

    public static String[] splitCSV(BufferedReader reader) throws IOException {
        return splitCSV(reader, null, ',', '"');
    }

    /**
     * @param reader          - some line enabled reader, we lazy
     * @param expectedColumns - convenient int[1] to return the expected
     * @param separator       - the C(omma) SV (or alternative like semi-colon)
     * @param quote           - double quote char ('"') or alternative
     * @return String[] containing the field
     * @throws IOException
     */
    public static String[] splitCSV(BufferedReader reader, int[] expectedColumns, char separator, char quote) throws IOException {
        final List<String> tokens = new ArrayList<String>(expectedColumns == null ? 8 : expectedColumns[0]);
        final StringBuilder sb = new StringBuilder(24);

        for (boolean quoted = false; ; sb.append('\n')) {//lazy, we do not preserve the original new line, but meh
            final String line = reader.readLine();
            if (line == null)
                break;
            for (int i = 0, len = line.length(); i < len; i++) {
                final char c = line.charAt(i);
                if (c == quote) {
                    if (quoted && i < len - 1 && line.charAt(i + 1) == quote) {//2xdouble quote in quoted
                        sb.append(c);
                        i++;//skip it
                    } else {
                        if (quoted) {
                            //next symbol must be either separator or eol according to RFC 4180
                            if (i == len - 1 || line.charAt(i + 1) == separator) {
                                quoted = false;
                                continue;
                            }
                        } else {//not quoted
                            if (sb.length() == 0) {//at the very start
                                quoted = true;
                                continue;
                            }
                        }
                        //if fall here, bogus, just add the quote and move on; or throw exception if you like to
                        /*
                        5.  Each field may or may not be enclosed in double quotes (however
                           some programs, such as Microsoft Excel, do not use double quotes
                           at all).  If fields are not enclosed with double quotes, then
                           double quotes may not appear inside the fields.
                      */
                        sb.append(c);
                    }
                } else if (c == separator && !quoted) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
            if (!quoted)
                break;
        }
        tokens.add(sb.toString());//add last
        if (expectedColumns != null)
            expectedColumns[0] = tokens.size();
        return tokens.toArray(new String[tokens.size()]);
    }

    public static String normalizeFileName(String name, char replace) {
        String invalidChars;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("windows") != -1) {
            invalidChars = "\\/:*?\"<>|";
        } else if (os.indexOf("mac") != -1) {
            invalidChars = "/:";
        } else { // assume Unix/Linux
            invalidChars = "/";
        }

        char[] chars = name.toCharArray();
        char[] norm = new char[chars.length];

        for (int i = 0; i < chars.length; i++) {
            if ((invalidChars.indexOf(chars[i]) >= 0) // OS-invalid
                    || (chars[i] < '\u0020') // ctrls
                    || (chars[i] > '\u007e' && chars[i] < '\u00a0') // ctrls
                    ) {
                norm[i] = replace;
            } else {
                norm[i] = chars[i];
            }
        }


        return new String(norm).trim();
    }

    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    }
}


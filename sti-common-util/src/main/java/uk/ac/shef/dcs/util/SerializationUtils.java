package uk.ac.shef.dcs.util;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 07/05/13
 * Time: 16:20
 */
public class SerializationUtils {

    public static String serializeBase64(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return Base64.encode(out.toByteArray());
    }
    public static Object deserializeBase64(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
}

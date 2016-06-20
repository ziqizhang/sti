package cz.cuni.mff.xrg.odalic.files;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileService {

  void create(File file);

  void create(File file, InputStream fileInputStream) throws IOException;
  
  void deleteById(String id);

  File getById(String id);
  
  List<File> getFiles();

  void replace(File file);
  
  void replace(File file, InputStream fileInputStream) throws IOException;

  boolean existsFileWithId(String id);

  boolean hasId(File file, String id);

  String getDataById(String id) throws IOException;
}

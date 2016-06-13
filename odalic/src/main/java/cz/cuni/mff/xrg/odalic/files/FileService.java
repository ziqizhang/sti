package cz.cuni.mff.xrg.odalic.files;

import java.util.List;

public interface FileService {

  void create(File file);

  void deleteById(String id);

  File getById(String id);
  
  List<File> getFiles();

  void replace(File file);

  File verifyFileExistenceById(String id);

  boolean hasId(File file, String id);

}

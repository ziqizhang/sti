/**
 * 
 */
package cz.cuni.mff.xrg.odalic.files;

import java.util.Collections;
import java.util.List;

/**
 * @author Václav Brodec
 *
 */
public final class MemoryOnlyFileService implements FileService {

  public MemoryOnlyFileService() {}

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#create(cz.cuni.mff.xrg.odalic.files.File)
   */
  public void create(File file) {
    // TODO Auto-generated method stub
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#deleteById(java.lang.String)
   */
  public void deleteById(String id) {
    // TODO Auto-generated method stub
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getById(java.lang.String)
   */
  public File getById(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getFiles()
   */
  public List<File> getFiles() {
    // TODO Auto-generated method stub
    return Collections.emptyList();
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#replace(cz.cuni.mff.xrg.odalic.files.File)
   */
  public void replace(File file) {
    // TODO Auto-generated method stub
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#verifyFileExistenceById(java.lang.String)
   */
  public File verifyFileExistenceById(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#hasId(cz.cuni.mff.xrg.odalic.files.File, java.lang.String)
   */
  public boolean hasId(File file, String id) {
    if (file.getId() == null) {
      return false;
    }
    
    return file.getId().equals(id);
  }

}

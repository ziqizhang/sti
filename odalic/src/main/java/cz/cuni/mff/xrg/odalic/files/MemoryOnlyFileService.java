/**
 * 
 */
package cz.cuni.mff.xrg.odalic.files;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * @author Václav Brodec
 *
 */
public final class MemoryOnlyFileService implements FileService {

  private final Map<String, File> files = new HashMap<>();
  
  private final Map<URL, String> data = new HashMap<>();
    

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#create(cz.cuni.mff.xrg.odalic.files.File)
   */
  public void create(File file) {
    if (existsFileWithId(file.getId())) {
      throw new IllegalArgumentException();
    }
    
    replace(file);
  }
  
  @Override
  public void create(File file, InputStream fileInputStream) throws IOException {
    if (existsFileWithId(file.getId())) {
      throw new IllegalArgumentException();
    }
    
    replace(file, fileInputStream);
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#deleteById(java.lang.String)
   */
  public void deleteById(String id) {
    Preconditions.checkNotNull(id);
    
    File file = this.files.remove(id);
    if (file == null) {
      throw new IllegalArgumentException();
    }
    
    this.data.remove(file.getLocation());
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getById(java.lang.String)
   */
  public File getById(String id) {
    Preconditions.checkNotNull(id);
    
    File file = this.files.get(id);
    if (file == null) {
      throw new IllegalArgumentException();
    }
    
    return file;
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getFiles()
   */
  public List<File> getFiles() {
    return ImmutableList.copyOf(this.files.values());
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#replace(cz.cuni.mff.xrg.odalic.files.File)
   */
  public void replace(File file) {
    this.files.put(file.getId(), file);    
  }
  

  @Override
  public void replace(File file, InputStream fileInputStream) throws IOException {
    this.files.put(file.getId(), file);
    this.data.put(file.getLocation(), IOUtils.toString(fileInputStream));
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#existsFileWithId(java.lang.String)
   */
  public boolean existsFileWithId(String id) {
    Preconditions.checkNotNull(id);
    
    return this.files.containsKey(id);
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#hasId(cz.cuni.mff.xrg.odalic.files.File, java.lang.String)
   */
  public boolean hasId(File file, String id) {
    Preconditions.checkNotNull(id);
    
    if (file.getId() == null) {
      return false;
    }
    
    return file.getId().equals(id);
  }
}

/**
 * 
 */
package cz.cuni.mff.xrg.odalic.files;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * This {@link FileService} implementation provides no persistence.
 * 
 * @author Václav Brodec
 *
 */
public final class MemoryOnlyFileService implements FileService {

  private final Map<String, File> files;
  
  private final Map<URL, String> data;
    
  private MemoryOnlyFileService(Map<String, File> files, Map<URL, String> data) {
    Preconditions.checkNotNull(files);
    Preconditions.checkNotNull(data);
    
    this.files = files;
    this.data = data;
  }
  
  /**
   *  Creates the file service with no registered files and data.
   */
  public MemoryOnlyFileService() {
    this(new HashMap<>(), new HashMap<>());
  }
  
  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#create(cz.cuni.mff.xrg.odalic.files.File)
   */
  @Override
  public void create(File file) {
    Preconditions.checkArgument(!existsFileWithId(file.getId()));
    
    replace(file);
  }
  
  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#create(cz.cuni.mff.xrg.odalic.files.File, java.io.InputStream)
   */
  @Override
  public void create(File file, InputStream fileInputStream) throws IOException {
    Preconditions.checkArgument(!existsFileWithId(file.getId()));
    
    replace(file, fileInputStream);
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#deleteById(java.lang.String)
   */
  @Override
  public void deleteById(String id) {
    Preconditions.checkNotNull(id);
    
    final File file = this.files.remove(id);
    Preconditions.checkArgument(file != null);
    
    this.data.remove(file.getLocation());
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getById(java.lang.String)
   */
  @Override
  public File getById(String id) {
    Preconditions.checkNotNull(id);
    
    final File file = this.files.get(id);
    Preconditions.checkArgument(file != null, "File does not exists!");
    
    return file;
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getFiles()
   */
  @Override
  public List<File> getFiles() {
    return ImmutableList.copyOf(this.files.values());
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#replace(cz.cuni.mff.xrg.odalic.files.File)
   */
  @Override
  public void replace(File file) {
    final File previous = this.files.get(file.getId());
    if (!previous.getLocation().equals(file.getLocation())) {
      this.data.remove(previous.getLocation());
    }
    
    this.files.put(file.getId(), file);
  }
  

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#replace(cz.cuni.mff.xrg.odalic.files.File, java.io.InputStream)
   */
  @Override
  public void replace(File file, InputStream fileInputStream) throws IOException {
    Preconditions.checkArgument(file.isCached());
    
    this.files.put(file.getId(), file);
    this.data.put(file.getLocation(), IOUtils.toString(fileInputStream, StandardCharsets.UTF_8));
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#existsFileWithId(java.lang.String)
   */
  @Override
  public boolean existsFileWithId(String id) {
    Preconditions.checkNotNull(id);
    
    return this.files.containsKey(id);
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#hasId(cz.cuni.mff.xrg.odalic.files.File, java.lang.String)
   */
  @Override
  public boolean hasId(File file, String id) {
    Preconditions.checkNotNull(id);
    
    return file.getId().equals(id);
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getDataById(java.lang.String)
   */
  @Override
  public String getDataById(String id) throws IOException {
    final File file = getById(id);
    
    final String data = this.data.get(file.getLocation());
    if (data == null) {
      return IOUtils.toString(file.getLocation(), StandardCharsets.UTF_8);
    } else {
      return data;
    }
  }
}

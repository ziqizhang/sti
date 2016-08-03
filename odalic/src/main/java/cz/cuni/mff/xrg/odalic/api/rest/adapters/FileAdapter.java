package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.FileValue;
import cz.cuni.mff.xrg.odalic.files.File;


public final class FileAdapter
    extends XmlAdapter<FileValue, File> {

  @Override
  public FileValue marshal(File bound)
      throws Exception {
    return new FileValue(bound);
  }

  @Override
  public File unmarshal(FileValue value)
      throws Exception {
    return new File(value.getId(), value.getUploaded(), value.getOwner(), value.getLocation());
  }
}

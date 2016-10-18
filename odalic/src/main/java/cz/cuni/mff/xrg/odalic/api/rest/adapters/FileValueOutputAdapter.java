package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.FileValueOutput;
import cz.cuni.mff.xrg.odalic.files.File;


public final class FileValueOutputAdapter
    extends XmlAdapter<FileValueOutput, File> {

  @Override
  public FileValueOutput marshal(File bound)
      throws Exception {
    return new FileValueOutput(bound);
  }

  @Override
  public File unmarshal(FileValueOutput value)
      throws Exception {
    throw new UnsupportedOperationException();
  }
}

package cz.cuni.mff.xrg.odalic.tasks.feedbacks;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.feedbacks.input.Input;
import cz.cuni.mff.xrg.odalic.feedbacks.input.InputParser;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;

/**
 * @author Václav Brodec
 *
 */
public final class MemoryOnlyFeedbackService implements FeedbackService {

  private final ConfigurationService configurationService;
  private final FileService fileService;
  private final InputParser inputParser;
  
  @Autowired
  public MemoryOnlyFeedbackService(ConfigurationService configurationService, FileService fileService, InputParser inputParser) {
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(inputParser);
    
    this.configurationService = configurationService;
    this.fileService = fileService;
    this.inputParser = inputParser;
  }

  @Override
  public Feedback getForTaskId(String taskId) {
    final Configuration configuration = configurationService.getForTaskId(taskId);
    
    return configuration.getFeedback();
  }

  @Override
  public void setForTaskId(String taskId, Feedback feedback) {
    final Configuration oldConfiguration = configurationService.getForTaskId(taskId);
    configurationService.setForTaskId(taskId, new Configuration(oldConfiguration.getInput(), feedback));
  }

  @Override
  public Input getInputForTaskId(String taskId) throws IOException {
    final Configuration configuration = configurationService.getForTaskId(taskId);
    final File file = configuration.getInput();
    final String data = fileService.getDataById(file.getId());
    
    return inputParser.parse(data);
  }
}

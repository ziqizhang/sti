/**
 * 
 */
package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.Immutable;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.tasks.feedbacks.FeedbackService;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * Implementation of {@link AnnotatedTableService} that gets the {@link AnnotatedTable} by adapting
 * present {@link Result}, {@link Input} and {@link Configuration} instances.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public final class ResultAdaptingAnnotatedTableService implements AnnotatedTableService {

  private final ExecutionService executionService;

  private final FeedbackService feedbackService;

  private final ConfigurationService configurationService;

  private final ResultToAnnotatedTableAdapter resultToAnnotatedTableAdapter;

  @Autowired
  public ResultAdaptingAnnotatedTableService(ExecutionService executionService,
      FeedbackService feedbackService, ConfigurationService configurationService,
      ResultToAnnotatedTableAdapter resultToAnnotatedTableAdapter) {
    Preconditions.checkNotNull(feedbackService);
    Preconditions.checkNotNull(executionService);
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(resultToAnnotatedTableAdapter);

    this.executionService = executionService;
    this.feedbackService = feedbackService;
    this.configurationService = configurationService;
    this.resultToAnnotatedTableAdapter = resultToAnnotatedTableAdapter;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTableService#getAnnotatedTableForTaskId(
   * java.lang.String)
   */
  @Override
  public AnnotatedTable getAnnotatedTableForTaskId(String id)
      throws IllegalArgumentException, CancellationException, InterruptedException, ExecutionException, IOException {
    final Result result = executionService.getResultForTaskId(id);
    final Input input = feedbackService.getInputForTaskId(id);
    final Configuration configuration = configurationService.getForTaskId(id);

    return resultToAnnotatedTableAdapter.toAnnotatedTable(result, input, configuration);
  }

}

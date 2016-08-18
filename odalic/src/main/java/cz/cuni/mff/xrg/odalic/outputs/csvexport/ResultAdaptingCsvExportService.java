/**
 * 
 */
package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.Immutable;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.tasks.feedbacks.FeedbackService;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * Implementation of {@link RdfExportService} that gets the extended CSV data by adapting present
 * {@link Result}, {@link Input} and {@link Configuration} instances.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public class ResultAdaptingCsvExportService implements CsvExportService {

  private final ExecutionService executionService;

  private final FeedbackService feedbackService;

  private final ConfigurationService configurationService;

  private final ResultToCSVExportAdapter resultToCsvExportAdapter;

  private final CSVExporter csvExporter;

  @Autowired
  public ResultAdaptingCsvExportService(ExecutionService executionService,
      FeedbackService feedbackService, ConfigurationService configurationService,
      ResultToCSVExportAdapter resultToCsvExportAdapter, CSVExporter csvExporter) {
    Preconditions.checkNotNull(feedbackService);
    Preconditions.checkNotNull(executionService);
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(resultToCsvExportAdapter);
    Preconditions.checkNotNull(csvExporter);

    this.executionService = executionService;
    this.feedbackService = feedbackService;
    this.configurationService = configurationService;
    this.resultToCsvExportAdapter = resultToCsvExportAdapter;
    this.csvExporter = csvExporter;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.outputs.csvexport.CsvExportService#getExtendedCsvForTaskId(java.lang.
   * String)
   */
  @Override
  public String getExtendedCsvForTaskId(String id)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    final Input output = getExtendedInputForTaskId(id);
    
    // TODO: Get the real used CSV configuration.
    final String data = csvExporter.export(output, new CsvConfiguration());

    return data;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.outputs.csvexport.CsvExportService#getExtendedInputForTaskId(java.lang.
   * String)
   */
  @Override
  public Input getExtendedInputForTaskId(String id)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    final Result result = executionService.getResultForTaskId(id);
    final Input input = feedbackService.getInputForTaskId(id);
    final Configuration configuration = configurationService.getForTaskId(id);

    final Input output = resultToCsvExportAdapter.toCSVExport(result, input, configuration);

    return output;
  }
}

/**
 * 
 */
package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.AnnotationToResultAdapter;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.xtractor.csv.TableXtractorCSV;

/**
 * @author Václav Brodec
 *
 */
public final class FutureBasedExecutionService implements ExecutionService {

  private final TaskService taskService;
  private final FileService fileService;
  private final AnnotationToResultAdapter annotationResultAdapter;
  private final SemanticTableInterpreterFactory semanticTableInterpreterFactory;
  private final TableXtractorCSV tableExtractor;
  private final ExecutorService executorService = Executors.newFixedThreadPool(1);
  private final Map<Task, Future<Result>> tasksToResults = new HashMap<>();

  @Autowired
  public FutureBasedExecutionService(TaskService taskService, FileService fileService,
      AnnotationToResultAdapter annotationToResultAdapter,
      SemanticTableInterpreterFactory semanticTableInterpreterFactory, TableXtractorCSV tableExtractor) {
    Preconditions.checkNotNull(taskService);
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(annotationToResultAdapter);
    Preconditions.checkNotNull(semanticTableInterpreterFactory);
    Preconditions.checkNotNull(tableExtractor);

    this.taskService = taskService;
    this.fileService = fileService;
    this.annotationResultAdapter = annotationToResultAdapter;
    this.semanticTableInterpreterFactory = semanticTableInterpreterFactory;
    this.tableExtractor = tableExtractor;
  }

  @Override
  public void submitForTaskId(String id) {
    final Task task = taskService.getById(id);

    final Future<Result> resultFuture = tasksToResults.get(task);
    if (resultFuture != null) {
      if (!resultFuture.isDone()) {
        throw new IllegalStateException();
      }
    }

    final Configuration configuration = task.getConfiguration();
    final File file = configuration.getInput();
    
    final Set<ColumnIgnore> columnIgnores = configuration.getFeedback().getColumnIgnores();
    final Integer[] columnIgnoresArray =
        columnIgnores.stream().map(e -> e.getPosition().getIndex()).toArray(Integer[]::new);
    
    final Callable<Result> execution = () -> {
      final String data = fileService.getDataById(file.getId());
      
      final java.io.File tempFile = java.io.File.createTempFile("odalic", "csv");
      tempFile.deleteOnExit();
      FileUtils.writeStringToFile(tempFile, data);
      
      final List<Table> tables = tableExtractor.extract(tempFile, tempFile.getName());
      if (tables.isEmpty()) {
        throw new IllegalArgumentException();
      }

      final SemanticTableInterpreter interpreter = semanticTableInterpreterFactory.getInterpreter();
      semanticTableInterpreterFactory.setIgnoreColumnsForInterpreter(columnIgnoresArray);

      final TAnnotation annotationResult = interpreter.start(tables.get(0), true);
      final Result result = annotationResultAdapter.toResult(annotationResult);

      return result;
    };

    final Future<Result> future = executorService.submit(execution);
    tasksToResults.put(task, future);
  }

  @Override
  public Result getResultForTaskId(String id) throws InterruptedException, ExecutionException {
    final Task task = taskService.getById(id);
    final Future<Result> resultFuture = tasksToResults.get(task);

    return resultFuture.get();
  }

  @Override
  public void cancelForTaskId(String id) {
    final Task task = taskService.getById(id);
    final Future<Result> resultFuture = tasksToResults.get(task);

    resultFuture.cancel(true);
  }

  @Override
  public boolean isDoneForTaskId(String id) {
    final Task task = taskService.getById(id);
    final Future<Result> resultFuture = tasksToResults.get(task);

    return resultFuture.isDone();
  }

  @Override
  public boolean isCancelledForTaskId(String id) {
    final Task task = taskService.getById(id);
    final Future<Result> resultFuture = tasksToResults.get(task);

    return resultFuture.isCancelled();
  }

  @Override
  public boolean hasBeenScheduledForTaskId(String id) {
    final Task task = taskService.getById(id);
    final Future<Result> resultFuture = tasksToResults.get(task);

    return resultFuture != null;
  }
}

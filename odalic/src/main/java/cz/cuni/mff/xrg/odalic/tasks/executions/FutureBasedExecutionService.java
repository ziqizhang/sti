/**
 * 
 */
package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import cz.cuni.mff.xrg.odalic.files.File;
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
  private final AnnotationToResultAdapter annotationResultAdapter;
  private final SemanticTableInterpreterFactory semanticTableInterpreterFactory;
  private final TableXtractorCSV tableExtractor;
  private final ExecutorService executorService = Executors.newFixedThreadPool(1);
  private final Map<Task, Future<Result>> tasksToResults = new HashMap<>();

  @Autowired
  public FutureBasedExecutionService(TaskService taskService,
      AnnotationToResultAdapter annotationToResultAdapter,
      SemanticTableInterpreterFactory semanticTableInterpreterFactory, TableXtractorCSV tableExtractor) {
    Preconditions.checkNotNull(taskService);
    Preconditions.checkNotNull(annotationToResultAdapter);
    Preconditions.checkNotNull(semanticTableInterpreterFactory);
    Preconditions.checkNotNull(tableExtractor);

    this.taskService = taskService;
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
    final URL fileLocation = file.getLocation();
    final java.io.File inputFile = urlToFile(fileLocation);

    final Set<ColumnIgnore> columnIgnores = configuration.getFeedback().getColumnIgnores();
    final Integer[] columnIgnoresArray =
        columnIgnores.stream().map(e -> e.getPosition().getIndex()).toArray(Integer[]::new);

    final Callable<Result> execution = () -> {
      final List<Table> tables = tableExtractor.extract(inputFile, inputFile.getName());
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

  private java.io.File urlToFile(final URL fileLocation) {
    java.io.File inputFile;
    try {
      inputFile = new java.io.File(fileLocation.toURI());
    } catch (URISyntaxException e) {
      inputFile = new java.io.File(fileLocation.getPath());
    }
    return inputFile;
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

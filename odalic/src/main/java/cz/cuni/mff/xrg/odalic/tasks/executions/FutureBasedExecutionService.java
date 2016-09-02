/**
 * 
 */
package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;
import cz.cuni.mff.xrg.odalic.input.CsvInputParser;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.InputToTableAdapter;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.AnnotationToResultAdapter;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 * <p>
 * Implementation of {@link ExecutionService} based on {@link Future} and {@link ExecutorService}
 * implementations.
 * </p>
 * 
 * <p>
 * Provides no persistence whatsoever
 * </p>
 * 
 * @author Václav Brodec
 *
 */
public final class FutureBasedExecutionService implements ExecutionService {

  private final TaskService taskService;
  private final FileService fileService;
  private final AnnotationToResultAdapter annotationResultAdapter;
  private final SemanticTableInterpreterFactory semanticTableInterpreterFactory;
  private final CsvInputParser csvInputParser;
  private final InputToTableAdapter inputToTableAdapter;
  private final ExecutorService executorService = Executors.newFixedThreadPool(1);
  private final Map<Task, Future<Result>> tasksToResults = new HashMap<>();

  @Autowired
  public FutureBasedExecutionService(TaskService taskService, FileService fileService,
      AnnotationToResultAdapter annotationToResultAdapter,
      SemanticTableInterpreterFactory semanticTableInterpreterFactory,
      CsvInputParser csvInputParser, InputToTableAdapter inputToTableAdapter) {
    Preconditions.checkNotNull(taskService);
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(annotationToResultAdapter);
    Preconditions.checkNotNull(semanticTableInterpreterFactory);
    Preconditions.checkNotNull(csvInputParser);
    Preconditions.checkNotNull(inputToTableAdapter);

    this.taskService = taskService;
    this.fileService = fileService;
    this.annotationResultAdapter = annotationToResultAdapter;
    this.semanticTableInterpreterFactory = semanticTableInterpreterFactory;
    this.csvInputParser = csvInputParser;
    this.inputToTableAdapter = inputToTableAdapter;
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService#submitForTaskId(java.lang.String)
   */
  @Override
  public void submitForTaskId(String id) throws IllegalStateException {
    final Task task = taskService.getById(id);

    final Future<Result> resultFuture = tasksToResults.get(task);
    Preconditions.checkState(resultFuture == null || resultFuture.isDone());

    final Configuration configuration = task.getConfiguration();
    final File file = configuration.getInput();

    final Set<ColumnIgnore> columnIgnores = configuration.getFeedback().getColumnIgnores();
    final KnowledgeBase primaryBase = configuration.getPrimaryBase();

    final Callable<Result> execution = () -> {
      final String data = fileService.getDataById(file.getId());

      // TODO: Read configuration attributed to the file instead of the default one.
      final Input input = csvInputParser.parse(data, file.getId(), new CsvConfiguration());
      final Table table = inputToTableAdapter.toTable(input);

      final Map<String, SemanticTableInterpreter> interpreters = semanticTableInterpreterFactory.getInterpreters();
      semanticTableInterpreterFactory.setColumnIgnoresForInterpreter(columnIgnores);

      Map<KnowledgeBase, TAnnotation> results = new HashMap<>();
      for (Map.Entry<String, SemanticTableInterpreter> interpreterEntry : interpreters.entrySet()) {
        final TAnnotation annotationResult = interpreterEntry.getValue().start(table, true);
        results.put(new KnowledgeBase(interpreterEntry.getKey()), annotationResult);
      }
      final Result result = annotationResultAdapter
          .toResult(results, primaryBase);

      return result;
    };

    final Future<Result> future = executorService.submit(execution);
    tasksToResults.put(task, future);
  }

  @Override
  public Result getResultForTaskId(String id)
      throws InterruptedException, ExecutionException, CancellationException, InterruptedException {
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
  public boolean isCanceledForTaskId(String id) {
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

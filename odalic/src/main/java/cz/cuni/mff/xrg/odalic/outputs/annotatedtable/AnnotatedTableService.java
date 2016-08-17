package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * This service handles provides the {@link AnnotatedTable} for existing {@link Result}.
 * 
 * @author Václav Brodec
 *
 */
public interface AnnotatedTableService {
  /**
   * Get the result of the task in the form of {@link AnnotatedTable}. 
   * 
   * @param id task ID
   * @return annotations
   * @throws InterruptedException if the execution was interrupted while waiting
   * @throws ExecutionException if the computation threw an exception
   * @throws CancellationException  if the computation was cancelled
   * @throws IOException if an I/O exception occurs when reading underlying data
   */
  AnnotatedTable getAnnotatedTableForTaskId(String id)
      throws CancellationException, InterruptedException, ExecutionException, IOException;
}

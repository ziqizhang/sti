package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import cz.cuni.mff.xrg.odalic.input.Input;

/**
 * Service providing the extended CSV (a part of the task result when using standard table
 * annotations).
 * 
 * @author Václav Brodec
 *
 */
public interface CsvExportService {
  /**
   * Gets a part of the task result in the form of extended CSV.
   * 
   * @param id task ID
   * @return extended CSV output
   * @throws InterruptedException if the execution was interrupted while waiting
   * @throws ExecutionException if the computation threw an exception
   * @throws CancellationException if the computation was cancelled
   * @throws IOException if an I/O exception occurs when creating the output content
   */
  String getExtendedCsvForTaskId(String id)
      throws CancellationException, InterruptedException, ExecutionException, IOException;

  /**
   * Gets a part of the task result in the form of extended input.
   * 
   * @param id task ID
   * @return extended input
   * @throws InterruptedException if the execution was interrupted while waiting
   * @throws ExecutionException if the computation threw an exception
   * @throws CancellationException if the computation was cancelled
   * @throws IOException if an I/O exception occurs when creating the output content
   */
  Input getExtendedInputForTaskId(String id)
      throws CancellationException, InterruptedException, ExecutionException, IOException;
}

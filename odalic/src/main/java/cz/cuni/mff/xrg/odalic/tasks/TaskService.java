package cz.cuni.mff.xrg.odalic.tasks;

import java.util.Set;

public interface TaskService {

  Set<Task> getTasks();

  Task getById(String id);

  boolean hasId(Task task, String id);

  void deleteById(String id);

  Task verifyTaskExistenceById(String id);

  void create(Task task);

  void replace(Task task);
}

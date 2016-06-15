package cz.cuni.mff.xrg.odalic.tasks;

import java.util.List;

public interface TaskService {

  List<TaskDigest> getTasks();

  Task getById(String id);

  boolean hasId(Task task, String id);

  void deleteById(String id);

  Task verifyTaskExistenceById(String id);

  void create(Task task);

  void replace(Task task);
}

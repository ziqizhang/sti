package cz.cuni.mff.xrg.odalic.tasks;

import java.util.Collections;
import java.util.List;

/**
 * @author Václav Brodec
 *
 */
public final class TaskServiceImpl implements TaskService {

  public TaskServiceImpl() {}
  
  public List<TaskDigest> getTasks() {
    return Collections.emptyList();
  }

  public Task getById(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasId(Task task, String id) {
    if (task.getId() == null) {
      return false;
    }
    
    return task.getId().equals(id);
  }

  public void deleteById(String id) {
    // TODO Auto-generated method stub
  }

  public Task verifyTaskExistenceById(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public void create(Task task) {
    // TODO Auto-generated method stub
  }

  public void replace(Task task) {
    // TODO Auto-generated method stub
  }
}

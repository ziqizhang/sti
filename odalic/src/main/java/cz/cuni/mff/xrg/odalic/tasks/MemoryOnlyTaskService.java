package cz.cuni.mff.xrg.odalic.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * This {@link TaskService} implementation provides no persistence.
 * 
 * @author Václav Brodec
 * @author Josef Janoušek
 *
 */
public final class MemoryOnlyTaskService implements TaskService {

  private final Map<String, Task> tasks;
  
  private MemoryOnlyTaskService(Map<String, Task> tasks) {
    Preconditions.checkNotNull(tasks);
    
    this.tasks = tasks;
  }
  
  /**
   * Creates the task service with no registered tasks.
   */
  public MemoryOnlyTaskService() {
    this(new HashMap<>());
  }
  
  public Set<Task> getTasks() {
    return ImmutableSet.copyOf(this.tasks.values());
  }

  public Task getById(String id) {
    Preconditions.checkNotNull(id);
    
    Task task = this.tasks.get(id);
    Preconditions.checkArgument(task != null);
    
    return task;
  }

  public boolean hasId(Task task, String id) {
    Preconditions.checkNotNull(id);
    
    if (task.getId() == null) {
      return false;
    }
    
    return task.getId().equals(id);
  }

  public void deleteById(String id) {
    Preconditions.checkNotNull(id);
    
    Task task = this.tasks.remove(id);
    Preconditions.checkArgument(task != null);
  }

  @Nullable
  public Task verifyTaskExistenceById(String id) {
    Preconditions.checkNotNull(id);
    
    if(this.tasks.containsKey(id)) {
      return this.tasks.get(id);
    }
    else {
      return null;
    }
  }

  public void create(Task task) {
    Preconditions.checkArgument(verifyTaskExistenceById(task.getId()) == null);
    
    replace(task);
  }

  public void replace(Task task) {
    this.tasks.put(task.getId(), task);
  }
}

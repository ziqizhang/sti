package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;

/**
 * Explicit {@link Task} execution state representation for REST API.
 * 
 * @author Václav Brodec
 *
 * @see ExecutionService
 */
@XmlType
@XmlEnum(String.class)
@XmlRootElement(name = "state")
public enum StateValue {
  /**
   * Task is specified, but not yet submitted for execution.
   */
  @XmlEnumValue("READY") READY,
  
  /**
   * Task is submitted for execution, but not done or canceled yet.
   */
  @XmlEnumValue("RUNNING") RUNNING,
  
  /**
   * Task execution has ended with success.
   */
  @XmlEnumValue("SUCCESS") SUCCESS,
  
  /**
   * Task execution has ended with warnings.
   */
  @XmlEnumValue("WARNING") WARNING,
  
  /**
   * Task execution has ended with an error.
   */
  @XmlEnumValue("ERROR") ERROR
}

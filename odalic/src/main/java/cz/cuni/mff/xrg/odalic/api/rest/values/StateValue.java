package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonFormat;

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
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StateValue {
  /**
   * Task is specified, but not yet submitted for execution.
   */
  @XmlEnumValue("READY") READY,
  
  /**
   * Task is submitted for execution, but not done or canceled yet.
   */
  @XmlEnumValue("SCHEDULED") SCHEDULED,
  
  /**
   * Task execution has been voluntarily canceled and no new is submitted.
   */
  @XmlEnumValue("CANCELLED") CANCELLED,
  
  /**
   * Task execution has ended (either with result or error).
   */
  @XmlEnumValue("FINISHED") FINISHED
}

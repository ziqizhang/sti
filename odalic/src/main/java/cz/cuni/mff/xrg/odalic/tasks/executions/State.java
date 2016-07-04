package cz.cuni.mff.xrg.odalic.tasks.executions;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum(String.class)
@XmlRootElement
public enum State {
  @XmlEnumValue("READY") READY,
  @XmlEnumValue("SCHEDULED") SCHEDULED,
  @XmlEnumValue("CANCELLED") CANCELLED,
  @XmlEnumValue("FINISHED") FINISHED
}

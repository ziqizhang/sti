package cz.cuni.mff.xrg.odalic.tasks.executions;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum(Integer.class)
public enum State {
  @XmlEnumValue("1") READY,
  @XmlEnumValue("2") SCHEDULED,
  @XmlEnumValue("3") CANCELLED,
  @XmlEnumValue("4") FINISHED
}

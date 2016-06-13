package cz.cuni.mff.xrg.odalic.tasks;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum(Integer.class)
public enum State {
  @XmlEnumValue("1") READY,
  @XmlEnumValue("2") RUNNING,
  @XmlEnumValue("3") CANCELED,
  @XmlEnumValue("4") FINISHED
}

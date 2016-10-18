package cz.cuni.mff.xrg.odalic.api.rest.responses;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The REST API {@link Reply} type.
 * 
 * @author Václav Brodec
 *
 * @see Message the message format
 */
@XmlType
@XmlEnum(String.class)
@XmlRootElement
public enum ReplyType {
  /**
   * Reply contains actual data.
   */
  @XmlEnumValue("DATA") DATA,
  
  /**
   * Reply contains no actual data, only a message.
   */
  @XmlEnumValue("ERROR") MESSAGE
}

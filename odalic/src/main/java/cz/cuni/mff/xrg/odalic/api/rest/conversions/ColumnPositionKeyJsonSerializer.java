package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * Map key JSON serializer for {@link ColumnPosition} instances.
 * 
 * @author Václav Brodec
 *
 */
public final class ColumnPositionKeyJsonSerializer extends JsonSerializer<ColumnPosition> {

  /*
   * (non-Javadoc)
   * 
   * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object,
   * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
   */
  @Override
  public void serialize(ColumnPosition value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
    jgen.writeFieldName(Integer.toString(value.getIndex()));
  }

}

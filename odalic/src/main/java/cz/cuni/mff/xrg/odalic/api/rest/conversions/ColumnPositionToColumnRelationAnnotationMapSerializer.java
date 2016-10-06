package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;

/**
 * A custom nested map JSON serializer.
 * 
 * @author Václav Brodec
 *
 */
public final class ColumnPositionToColumnRelationAnnotationMapSerializer
    extends JsonSerializer<Map<ColumnPosition, ColumnRelationAnnotation>> {

  @Override
  public void serialize(Map<ColumnPosition, ColumnRelationAnnotation> value, JsonGenerator jgen,
      SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeStartObject();
    for (final Map.Entry<ColumnPosition, ColumnRelationAnnotation> entry : value.entrySet()) {
      jgen.writeObjectField(Integer.toString(entry.getKey().getIndex()), entry.getValue());
    }
    jgen.writeEndObject();
  }

}

package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellRelationAnnotation;

/**
 * A custom nested map JSON serializer.
 * 
 * @author Václav Brodec
 *
 */
public final class ColumnPositionToColumnPositionToCellRelationAnnotationMapMapSerializer
extends JsonSerializer<Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> {

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>> value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
      jgen.writeStartObject();
      for (final Map.Entry<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>> entry : value.entrySet()) {
        jgen.writeFieldName(Integer.toString(entry.getKey().getIndex()));
        
        jgen.writeStartObject();
        for (final Map.Entry<ColumnPosition, CellRelationAnnotation> subEntry : entry.getValue().entrySet()) {
          jgen.writeObjectField(Integer.toString(subEntry.getKey().getIndex()), subEntry.getValue());
        }
        jgen.writeEndObject();
      }
      jgen.writeEndObject();
    }

}

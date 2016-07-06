package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellRelationAnnotation;

/**
 * A custom nested map JSON deserializer.
 * 
 * @author Václav Brodec
 *
 */
public final class ColumnPositionToColumnPositionToCellRelationAnnotationMapMapDeserializer extends JsonDeserializer<Map<ColumnPosition, CellRelationAnnotation>> {

  /* (non-Javadoc)
   * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
   */
  @Override
  public Map<ColumnPosition, CellRelationAnnotation> deserialize(JsonParser parser,
      DeserializationContext ctxt) throws IOException, JsonProcessingException {
    throw new UnsupportedOperationException();
  }
}

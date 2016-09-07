package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import cz.cuni.mff.xrg.odalic.api.rest.values.EntityCandidateValue;

/**
 * A custom JSON serializer of a set of entity candidates.
 * 
 * @author Václav Brodec
 *
 */
public final class EntityCandidateValueSetSerializer
    extends JsonSerializer<Set<EntityCandidateValue>> {

  @Override
  public void serialize(Set<EntityCandidateValue> value, JsonGenerator jgen,
      SerializerProvider provider) throws IOException, JsonProcessingException {
    jgen.writeStartArray();
    for (final EntityCandidateValue entry : value) {
      jgen.writeObject(entry);
    }
    jgen.writeEndArray();
  }

}

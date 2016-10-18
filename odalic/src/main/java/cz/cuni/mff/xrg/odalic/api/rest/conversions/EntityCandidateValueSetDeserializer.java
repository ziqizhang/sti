package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.ImmutableSet;
import cz.cuni.mff.xrg.odalic.api.rest.values.EntityCandidateValue;

/**
 * A custom JSON deserializer of a set with entity candidates.
 * 
 * @author Václav Brodec
 *
 */
public final class EntityCandidateValueSetDeserializer
    extends JsonDeserializer<Set<EntityCandidateValue>> {

  /*
   * (non-Javadoc)
   * 
   * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.
   * JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
   */
  @Override
  public Set<EntityCandidateValue> deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    final EntityCandidateValue[] array = ctxt.readValue(parser, EntityCandidateValue[].class);
    
    return ImmutableSet.copyOf(array);
  }
}

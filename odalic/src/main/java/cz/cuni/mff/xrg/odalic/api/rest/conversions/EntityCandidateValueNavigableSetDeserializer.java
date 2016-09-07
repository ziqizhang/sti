package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import java.util.Iterator;
import java.util.NavigableSet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.values.EntityCandidateValue;

/**
 * A custom JSON deserializer of navigable set with entity candidates.
 * 
 * @author Václav Brodec
 *
 */
public final class EntityCandidateValueNavigableSetDeserializer
    extends JsonDeserializer<NavigableSet<EntityCandidateValue>> {

  /*
   * (non-Javadoc)
   * 
   * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.
   * JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
   */
  @Override
  public NavigableSet<EntityCandidateValue> deserialize(JsonParser parser,
      DeserializationContext ctxt) throws IOException, JsonProcessingException {
    final Iterator<EntityCandidateValue> entriesIterator =
        parser.readValuesAs(EntityCandidateValue.class);

    return ImmutableSortedSet.copyOf(entriesIterator);
  }
}

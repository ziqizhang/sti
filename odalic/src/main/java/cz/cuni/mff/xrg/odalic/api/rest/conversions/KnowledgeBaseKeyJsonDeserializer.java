package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

public class KnowledgeBaseKeyJsonDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) {
      return new KnowledgeBase(key);
    }
}
package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import cz.cuni.mff.xrg.odalic.positions.RowPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Map key JSON serializer for {@link KnowledgeBase} instances.
 * 
 * @author Václav Brodec
 *
 */
public final class RowPositionKeyJsonSerializer extends JsonSerializer<RowPosition> {

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(RowPosition value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
        jgen.writeFieldName(Integer.toString(value.getIndex()));       
    }

}

package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Serializer to custom date format {@value #DATE_FORMAT}.
 * 
 * @author Václav Brodec
 *
 */
public final class CustomDateJsonSerializer extends JsonSerializer<Date> {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    @Override
    public void serialize(Date value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        final String dateString = dateFormat.format(value);
        jgen.writeString(dateString);       
    }

}

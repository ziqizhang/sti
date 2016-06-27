package cz.cuni.mff.xrg.odalic.api.rest.conversions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class CustomJsonDateSerializer extends JsonSerializer<Date> {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    @Override
    public void serialize(Date value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
        
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String dateString = dateFormat.format(value);
        jgen.writeString(dateString);       
    }

}

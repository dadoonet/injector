package fr.pilato.elasticsearch.injector.serializer;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Jackson serializer for {@link Date} to ISO date string (yyyy-MM-dd).
 */
public class CustomDateSerializer extends JsonSerializer<Date> {

    /** Default constructor for Jackson. */
    public CustomDateSerializer() {
    }

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider arg2) throws IOException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = formatter.format(value);

        gen.writeString(formattedDate);
    }
}

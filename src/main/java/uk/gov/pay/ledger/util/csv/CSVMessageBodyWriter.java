package uk.gov.pay.ledger.util.csv;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@Provider
@Produces("text/csv")
public class CSVMessageBodyWriter implements MessageBodyWriter<List> {

    @Override
    public boolean isWriteable(Class targetType, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return List.class.isAssignableFrom(targetType);
    }

    @Override
    public long getSize(List data, Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        // https://docs.oracle.com/javaee/7/api/javax/ws/rs/ext/MessageBodyWriter.html
        return -1;
    }

    @Override
    public void writeTo(List data, Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        if (data != null && !data.isEmpty()) {
            CsvMapper mapper = new CsvMapper();

            mapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
            mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

            CsvSchema.Builder builder = CsvSchema.builder();
            Map<String, Object> headerRow = (Map<String, Object>) data.get(data.size() - 1); // get last record which is header
            headerRow.keySet().forEach(columnName -> builder.addColumn(columnName));

            CsvSchema schema = builder.build().withHeader();
            data.remove(data.size() - 1);
            mapper.writer(schema).writeValue(outputStream, data);
        }
    }
}
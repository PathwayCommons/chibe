package cpath.client.util;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.model.Model;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts the cpath2 REST response into a BioPAX model.
 */
public class BioPAXHttpMessageConverter implements HttpMessageConverter<Model> {
    private final BioPAXIOHandler bioPAXIOHandler;
    private static final List<MediaType> mediaList;
    
    static {
    	mediaList = new ArrayList<MediaType>();
        mediaList.add(MediaType.TEXT_PLAIN);
        mediaList.add(MediaType.parseMediaType("application/vnd.biopax.rdf+xml"));
    }

    public BioPAXHttpMessageConverter(BioPAXIOHandler bioPAXIOHandler) {
        this.bioPAXIOHandler = bioPAXIOHandler;
    }

    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return Model.class.equals(clazz);
    }

    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    public List<MediaType> getSupportedMediaTypes() {
        return Collections.unmodifiableList(mediaList);
    }

    public Model read(Class<? extends Model> clazz, HttpInputMessage inputMessage) throws IOException
    {
        /* OK, a little bit of hacking here:

           The PC2 server returns either a valid BioPAX model or an error coded in XML.
           So if the BioPAX IO Handler fails, we have to re-read the stream from the
           beginning in order to parse the error details.

           This is why we copy the stream into a buffered one.
         */    	
    	
        BufferedInputStream bis = new BufferedInputStream(inputMessage.getBody());
        bis.mark(0);

        return bioPAXIOHandler.convertFromOWL(bis);
    }

    public void write(Model model, MediaType contentType, HttpOutputMessage outputMessage) throws IOException
    {
        throw new UnsupportedOperationException("Not supported");
    }
}

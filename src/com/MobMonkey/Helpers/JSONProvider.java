/*package com.MobMonkey.Helpers;

import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.springframework.stereotype.Component;



@Provider
@Component
public class JSONProvider extends JacksonJsonProvider {
	private ObjectMapper mapper = new ObjectMapper();

	public JSONProvider() {
		*//** without setting this, StdDeserializer not found exception occurs *//*
		AnnotationIntrospector primary = new JaxbAnnotationIntrospector();
		AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
		AnnotationIntrospector pair = new AnnotationIntrospector.Pair(primary,
				secondary);
		mapper.setAnnotationIntrospector(pair);
		mapper.configure(Feature.INDENT_OUTPUT, true);
		setMapper(mapper);
	}
}
*/
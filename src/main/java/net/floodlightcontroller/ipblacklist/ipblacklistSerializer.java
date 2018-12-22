package net.floodlightcontroller.ipblacklist;

import java.io.IOException;
import java.net.InetAddress;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;



public class ipblacklistSerializer extends JsonSerializer<InetAddress>{

	@Override
	public void serialize(InetAddress ip, JsonGenerator jGen, SerializerProvider serializer)
			throws IOException, JsonProcessingException {
		
		jGen.writeStartObject();
		jGen.writeStringField("ip", ip.getHostAddress());
		jGen.writeEndObject();
		
	}

}

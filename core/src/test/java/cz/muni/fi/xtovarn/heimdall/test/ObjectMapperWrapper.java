package cz.muni.fi.xtovarn.heimdall.test;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class ObjectMapperWrapper extends ObjectMapper {

	private static final long serialVersionUID = 1L;

	public byte[] writeValueAsBytesNoExceptions(Object value) {
		try {
			return super.writeValueAsBytes(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public <T> T readValueNoExceptions(byte[] src, Class<T> valueType) {
		try {
			return super.readValue(src, valueType);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
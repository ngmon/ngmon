package cz.muni.fi.xtovarn.heimdall.commons.entity;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sleepycat.persist.model.Persistent;

import java.util.LinkedHashMap;
import java.util.Map;

@Persistent
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.WRAPPER_OBJECT)
public class Payload {

	@JsonTypeId
	private String schema;

	@JsonIgnore
	private String schemaVersion;

	public Payload() {
	}

	private Map<String, Object> properties = new LinkedHashMap<String, Object>(0);

	@JsonAnySetter
	public void add(String key, Object value) {
		properties.put(key, value);
	}

	@JsonAnyGetter
	public Map<String, Object> properties() {
		return properties;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public Object getValue(String name) {
		return properties.get(name);
	}

	@Override
	public String toString() {
		return "[Payload@" + this.hashCode()  + "] {" +
				"schema='" + schema + '\'' +
				", schemaVersion='" + schemaVersion + '\'' +
				", properties=" + properties +
				'}';
	}
}

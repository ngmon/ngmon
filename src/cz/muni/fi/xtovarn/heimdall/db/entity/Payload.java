package cz.muni.fi.xtovarn.heimdall.db.entity;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.Map;

public class Payload {
	private String schema;
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

	@Override
	public String toString() {
		return "Payload{" +
				"schema='" + schema + '\'' +
				", schemaVersion='" + schemaVersion + '\'' +
				", properties=" + properties +
				'}';
	}
}
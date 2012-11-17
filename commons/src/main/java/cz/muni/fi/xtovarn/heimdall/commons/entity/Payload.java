package cz.muni.fi.xtovarn.heimdall.commons.entity;

import com.fasterxml.jackson.annotation.*;
import com.sleepycat.persist.model.Persistent;

import java.util.LinkedHashMap;
import java.util.Map;

@Persistent
public class Payload {

	private String version;

	public Payload() {
	}

	private Map<String, Object> properties = new LinkedHashMap<>(1);

	@JsonAnySetter
	public void add(String key, Object value) {
		properties.put(key, value);
	}

	@JsonAnyGetter
	public Map<String, Object> properties() {
		return properties;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Object getValue(String name) {
		return properties.get(name);
	}

	@Override
	public String toString() {
		return "Payload{" +
				"version='" + version + '\'' +
				", properties=" + properties +
				'}';
	}
}

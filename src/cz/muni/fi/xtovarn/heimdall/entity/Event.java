package cz.muni.fi.xtovarn.heimdall.entity;

import cz.muni.fi.xtovarn.heimdall.entity.Payload;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

import java.io.Serializable;
import java.util.Date;

@JsonRootName("Event")
public class Event {
	
	private long id;
	private Date time;
	private String hostname;
	private String type;
	private String application;
	private String process;
	private String processId;
	private int severity;
	private int priority;

	@JsonProperty("Payload")
	private Payload payload;

	public Event(){}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Payload getPayload() {
		return payload;
	}

	public void setPayload(Payload payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "Event{" +
				"id=" + id +
				", time=" + time +
				", hostname='" + hostname + '\'' +
				", type='" + type + '\'' +
				", application='" + application + '\'' +
				", process='" + process + '\'' +
				", processId='" + processId + '\'' +
				", severity=" + severity +
				", priority=" + priority +
				", payload=" + payload +
				'}';
	}
}

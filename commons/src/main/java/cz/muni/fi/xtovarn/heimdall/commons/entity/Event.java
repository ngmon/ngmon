package cz.muni.fi.xtovarn.heimdall.commons.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import java.util.Date;
import java.util.Set;

@Entity
@JsonRootName("Event")
@JsonPropertyOrder({"occurrenceTime", "type", "_"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Event {

	@PrimaryKey(sequence = "event_long_sequence")
	private long id;

	@SecondaryKey(name = "occurrenceTime", relate = Relationship.MANY_TO_ONE)
	private Date occurrenceTime;

	@SecondaryKey(name = "detectionTime", relate = Relationship.MANY_TO_ONE)
	private Date detectionTime;

	@SecondaryKey(name = "hostname", relate = Relationship.MANY_TO_ONE)
	private String hostname;

	@SecondaryKey(name = "type", relate = Relationship.MANY_TO_ONE)
	private String type;

	@SecondaryKey(name = "application", relate = Relationship.MANY_TO_ONE)
	private String application;

	@SecondaryKey(name = "process", relate = Relationship.MANY_TO_ONE)
	private String process;

	@SecondaryKey(name = "processId", relate = Relationship.MANY_TO_ONE)
	private int processId;

	@SecondaryKey(name = "level", relate = Relationship.MANY_TO_ONE)
	private int level;

	@SecondaryKey(name = "priority", relate = Relationship.MANY_TO_ONE)
	private int priority;

    @SecondaryKey(name = "tags", relate = Relationship.MANY_TO_MANY)
    private Set<String> tags;

	@JsonProperty("_")
	private Payload payload;

	public Event(){
		this.payload = new Payload();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getOccurrenceTime() {
		return occurrenceTime;
	}

	public void setOccurrenceTime(Date occurrenceTime) {
		this.occurrenceTime = occurrenceTime;
	}

	public Date getDetectionTime() {
		return detectionTime;
	}

	public void setDetectionTime(Date detectionTime) {
		this.detectionTime = detectionTime;
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

	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
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
		return "[Event@" + this.hashCode() + "] {" +
				"id=" + id +
				", occurrenceTime=" + occurrenceTime +
				", detectionTime=" + detectionTime +
				", hostname='" + hostname + '\'' +
				", type='" + type + '\'' +
				", application='" + application + '\'' +
				", process='" + process + '\'' +
				", processId=" + processId +
				", level=" + level +
				", priority=" + priority +
				", payload=" + payload +
				", tags=" + tags +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Event event = (Event) o;

		if (id != event.id) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}

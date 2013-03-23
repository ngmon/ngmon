package cz.muni.fi.xtovarn.heimdall.commons.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import cz.muni.fi.publishsubscribe.countingtree.Attribute;
import cz.muni.fi.publishsubscribe.countingtree.AttributeValue;

@Entity
@JsonRootName("Event")
@JsonPropertyOrder({"occurrenceTime", "type", "_"})
public class Event implements cz.muni.fi.publishsubscribe.countingtree.Event {

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
	private String processId;

	@SecondaryKey(name = "level", relate = Relationship.MANY_TO_ONE)
	private int level;

	@SecondaryKey(name = "priority", relate = Relationship.MANY_TO_ONE)
	private int priority;

	@JsonProperty("_")
	private Payload payload;
	
	private List<Attribute<? extends Comparable<?>>> attributes = new ArrayList<>();

	public Event(){
		this.payload = new Payload();
	}
	
	private <TA extends Comparable<TA>> void addAttribute(String name, Class<TA> type, TA value) {
		attributes.add(new Attribute<TA>(name, new AttributeValue<TA>(value, type)));
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
		addAttribute("Id", Long.class, id);
	}

	public Date getOccurrenceTime() {
		return occurrenceTime;
	}

	public void setOccurrenceTime(Date occurrenceTime) {
		this.occurrenceTime = occurrenceTime;
		addAttribute("occurenceTime", Date.class, occurrenceTime);
	}

	public Date getDetectionTime() {
		return detectionTime;
	}

	public void setDetectionTime(Date detectionTime) {
		this.detectionTime = detectionTime;
		addAttribute("detectionTime", Date.class, detectionTime);
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
		addAttribute("hostname", String.class, hostname);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		addAttribute("type", String.class, type);
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
		addAttribute("application", String.class, application);
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
		addAttribute("process", String.class, process);
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
		addAttribute("processId", String.class, processId);
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
		addAttribute("level", Long.class, new Long(level));
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
		addAttribute("priority", Long.class, new Long(priority));
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
				", processId='" + processId + '\'' +
				", level=" + level +
				", priority=" + priority +
				", payload=" + payload +
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

	@Override
	public List<Attribute<? extends Comparable<?>>> getAttributes() {
		// TODO - check there are no duplicate attributes
		// (happens if some set...() method is called more than once)
		return attributes;
	}
}

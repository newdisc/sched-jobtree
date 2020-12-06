package nd.sched.trigger;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Trigger {
	private String name;
	private String qualifier;
	private String job;
	private String arguments;
	private String parent;
	private String dependencies;
	//Computed fields:
	@JsonIgnore
	private List<Trigger> interested = new ArrayList<>();
	private TriggerStatus status;

	public String getJob() {
		return job;
	}
	public Trigger setJob(String job) {
		this.job = job;
		return this;
	}
	public String getArguments() {
		return arguments;
	}
	public Trigger setArguments(String arguments) {
		this.arguments = arguments;
		return this;
	}
	public String getQualifier() {
		return qualifier;
	}
	public Trigger setQualifier(String qualifier) {
		this.qualifier = qualifier;
		return this;
	}
	public String getName() {
		return name;
	}
	public Trigger setName(String name) {
		this.name = name;
		return this;
	}
	public String getParent() {
		return parent;
	}
	public Trigger setParent(String parent) {
		this.parent = parent;
		return this;
	}
	public List<Trigger> getInterested() {
		return interested;
	}
	public String getDependencies() {
		return dependencies;
	}
	public Trigger setDependencies(String dependencies) {
		this.dependencies = dependencies;
		return this;
	}
	public TriggerStatus getStatus() {
		return status;
	}
	public void setStatus(TriggerStatus status) {
		this.status = status;
	}
	public Trigger addInterested(final Trigger trig) {
		interested.add(trig);
		return this;
	}
}

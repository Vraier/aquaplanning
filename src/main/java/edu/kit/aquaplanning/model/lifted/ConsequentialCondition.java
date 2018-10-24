package edu.kit.aquaplanning.model.lifted;

import java.util.ArrayList;
import java.util.List;

public class ConsequentialCondition extends AbstractCondition {

	private List<Condition> prerequisites;
	private List<Condition> consequences;
	
	public ConsequentialCondition() {
		super(ConditionType.consequential);
		this.prerequisites = new ArrayList<>();
		this.consequences = new ArrayList<>();
	}
	
	public void addPrerequisite(Condition cond) {
		this.prerequisites.add(cond);
	}
	
	public void addConsequence(Condition cond) {
		this.consequences.add(cond);
	}
	
	public List<Condition> getPrerequisites() {
		return prerequisites;
	}
	
	public List<Condition> getConsequences() {
		return consequences;
	}
	
	@Override
	public ConsequentialCondition getConditionBoundToArguments(List<Argument> refArgs, List<Argument> argValues) {
		
		ConsequentialCondition c = new ConsequentialCondition();
		for (Condition cond : prerequisites) {
			c.addPrerequisite(cond.getConditionBoundToArguments(refArgs, argValues));
		}
		for (Condition cond : consequences) {
			c.addConsequence(cond.getConditionBoundToArguments(refArgs, argValues));
		}
		return c;
	}
	
	@Override
	public String toString() {
		String out = "";
		out += "{ ";
		for (Condition c : prerequisites) {
			out += c + " ";
		}
		out += "} => { ";
		for (Condition c : consequences) {
			out += c + " ";
		}
		out += "}";
		return out;
	}
}

package edu.kit.aquaplanning.model.lifted;

import java.util.ArrayList;
import java.util.List;

public class Operator {

	private String name;
	private List<Argument> arguments;
	private List<AbstractCondition> preconditions;
	private List<AbstractCondition> effects;
	private int cost;
	
	public Operator(String name) {
		this.name = name;
		arguments = new ArrayList<>();
		preconditions = new ArrayList<>();
		effects = new ArrayList<>();
		this.cost = 0;
	}
	
	public void addArgument(Argument arg) {
		this.arguments.add(arg);
	}
	
	public void addPrecondition(Condition cond) {
		this.preconditions.add(cond);
	}
	
	public void addEffect(Condition cond) {
		this.effects.add(cond);
	}
	
	public void addConditionalEffect(ConsequentialCondition cond) {
		this.effects.add(cond);
	}
	
	public void addQuantifiedPrecondition(Quantification q) {
		this.preconditions.add(q);
	}
	
	public void addQuantifiedEffect(Quantification q) {
		this.effects.add(q);
	}
	
	public void setCost(int cost) {
		this.cost = cost;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Argument> getArguments() {
		return arguments;
	}
	
	public List<Type> getArgumentTypes() {
		
		List<Type> argTypes = new ArrayList<>();
		arguments.forEach(arg -> argTypes.add(arg.getType()));
		return argTypes;
	}
	
	public List<AbstractCondition> getPreconditions() {
		return preconditions;
	}
	
	public List<AbstractCondition> getEffects() {
		return effects;
	}
	
	public int getCost() {
		return cost;
	}
	
	@Override
	public String toString() {
		String out = "";
		out += name + "( ";
		for (Argument arg : arguments) {
			out += arg.toString() + " ";
		}
		out += ")";
		if (cost != 0) {			
			out += "[cost:" + cost + "]";
		}
		out += " PRE: { ";
		for (AbstractCondition c : preconditions) {
			out += c + " ";
		}
		out += "}";
		out += " POST: { ";
		for (AbstractCondition c : effects) {
			out += c + " ";
		}
		out += "}";
		return out;
	}
}
package edu.kit.aquaplanning.grounding;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.lifted.Operator;
import edu.kit.aquaplanning.model.lifted.PlanningProblem;
import edu.kit.aquaplanning.model.lifted.Quantification;
import edu.kit.aquaplanning.model.lifted.Quantification.Quantifier;
import edu.kit.aquaplanning.model.lifted.AbstractCondition;
import edu.kit.aquaplanning.model.lifted.AbstractCondition.ConditionType;
import edu.kit.aquaplanning.model.lifted.Argument;
import edu.kit.aquaplanning.model.lifted.Condition;
import edu.kit.aquaplanning.model.lifted.ConditionSet;
import edu.kit.aquaplanning.model.lifted.ConsequentialCondition;
import edu.kit.aquaplanning.model.lifted.DerivedCondition;
import edu.kit.aquaplanning.model.lifted.DerivedPredicate;
import edu.kit.aquaplanning.model.lifted.Implication;
import edu.kit.aquaplanning.model.lifted.Negation;

/**
 * Provides simplification routines for lifted planning problems,
 * in particular for the structure of logical expressions and the
 * resolution of quantifications.
 */
public class Preprocessor {

	private Configuration config;
	private PlanningProblem problem;
	
	public Preprocessor(Configuration config) {
		this.config = config;
	}
	
	/**
	 * Does an in-place preprocessing of the problem at hand.
	 * Depending on the known configuration, all logical expressions
	 * are either simplified into a structure where negations occur
	 * only on an atomic level, or they are even converted into
	 * disjunctive normal form (DNF). Quantifications are always
	 * resolved (using the constants defined in the problem).
	 */
	public void preprocess(PlanningProblem problem) {
		
		this.problem = problem;
		
		// Resolve derived predicates
		if (config.substituteDerivedPredicates) {			
			resolveDerivedPredicates();
		}
		
		// Eliminate quantifications,
		// Simplify structure of logical expressions,
		// Convert to DNF, if desired
		boolean convertToDNF = !config.keepDisjunctions;
		simplifyProblem(convertToDNF);
		if (convertToDNF) {
			// Split DNF operators w.r.t. their preconditions
			// into new, simple operators
			List<Operator> newOperators = new ArrayList<>();
			for (Operator op : problem.getOperators()) {
				newOperators.addAll(split(op));
			}
			problem.getOperators().clear();
			problem.getOperators().addAll(newOperators);
		}
	}
	
	/**
	 * Simplifies the structure of all logical expressions in the problem.
	 */
	private void simplifyProblem(boolean toDNF) {
		
		// Simplify operators
		List<Operator> operators = new ArrayList<>();
		for (Operator op : problem.getOperators()) {
			
			// Preconditions
			AbstractCondition pre = op.getPrecondition();
			pre = instantiateQuantifications(pre);
			pre = pre.simplify(/*negated = */false);
			if (toDNF) {
				pre = pre.getDNF();
			}
			op.setPrecondition(pre);
			
			// Effects
			AbstractCondition eff = op.getEffect();
			eff = instantiateQuantifications(eff);
			eff = eff.simplify(/*negated = */false);
			if (toDNF) {
				eff = eff.getDNF();
			}
			op.setEffect(eff);
			
			operators.add(op);
		}
		problem.getOperators().clear();
		problem.getOperators().addAll(operators);
		
		// Simplify goal
		ConditionSet goalSet = new ConditionSet(ConditionType.conjunction);
		for (AbstractCondition cond : problem.getGoals()) {
			goalSet.add(cond);
		}
		AbstractCondition newGoal = instantiateQuantifications(goalSet).simplify(false);
		if (toDNF) {
			newGoal = newGoal.getDNF();
		}
		problem.getGoals().clear();
		problem.getGoals().add(newGoal);
	}
	
	/**
	 * Resolves all quantifications occurring in the provided condition.
	 * Existential quantifications are replaced with a disjunction and
	 * universal quantifications are replaced with a conjunction.
	 * Quantified variables in nested conditions are resolved for each
	 * possible constant defined in the problem.
	 */
	private AbstractCondition instantiateQuantifications(AbstractCondition cond) {
		
		switch (cond.getConditionType()) {
		
		// All cases besides quantification: just propagating down
		case atomic:
			if (cond instanceof DerivedCondition) {
				cond = cond.copy();
				AbstractCondition inner = ((DerivedCondition) cond).getPredicate().getCondition();
				((DerivedCondition) cond).getPredicate().setCondition(
					instantiateQuantifications(inner)
				);
				return cond;
			}
			return cond.copy();
		case negation:
			Negation n = new Negation();
			n.setChildCondition(instantiateQuantifications(
					((Negation) cond).getChildCondition()));
			return n;
		case conjunction:
		case disjunction:
			ConditionSet set = new ConditionSet(cond.getConditionType());
			for (AbstractCondition c : ((ConditionSet) cond).getConditions()) {
				set.add(instantiateQuantifications(c));
			}
			return set;
		case consequential:
			ConsequentialCondition condCC = (ConsequentialCondition) cond;
			ConsequentialCondition cc = new ConsequentialCondition();
			cc.setPrerequisite(instantiateQuantifications(condCC.getPrerequisite()));
			cc.setConsequence(instantiateQuantifications(condCC.getConsequence()));
			return cc;
		case implication:
			Implication condImp = (Implication) cond;
			Implication i = new Implication();
			i.setIfCondition(instantiateQuantifications(condImp.getIfCondition()));
			i.setThenCondition(instantiateQuantifications(condImp.getThenCondition()));
			return i;
			
		case quantification:
			Quantification q = (Quantification) cond;
			
			// Instantiate inner quantifications FIRST
			AbstractCondition innerCondition = instantiateQuantifications(q.getCondition());
			
			// New, dequantified condition
			ConditionType type = null;
			if (q.getQuantifier().equals(Quantifier.universal)) {
				type = ConditionType.conjunction; // forall: big AND
			} else {
				type = ConditionType.disjunction; // exists: big OR
			}
			ConditionSet dequantifiedSet = new ConditionSet(type);
			
			// For each quantified variable, replace its occurrences 
			// with all possible constants
			List<List<Argument>> eligibleArgs = 
			ArgumentCombination.getEligibleArguments(q.getVariables(), 
					problem, problem.getConstants());
			ArgumentCombination.iterator(eligibleArgs).forEachRemaining(args -> {
				
				AbstractCondition deq = innerCondition.getConditionBoundToArguments(
						q.getVariables(), args);
				dequantifiedSet.add(deq);
			});
			
			return dequantifiedSet;
			
		default:
			return null;
		}
	}
	
	/**
	 * Given an operator in DNF, creates a new operator for each
	 * element of the disjunction. This leads to a set of operators
	 * which together can achieve exactly the same as the old operator.
	 * Conditional effects are split up into multiple effects
	 * in a similar manner.
	 */
	private List<Operator> split(Operator op) {
		
		List<Operator> newOperators = new ArrayList<>();
		
		// Compute flat conditional effects
		ConditionSet effectSet = new ConditionSet(ConditionType.conjunction);
		AbstractCondition effect = effectSet;
		if (op.getEffect().getConditionType() == ConditionType.consequential) {
			// Top level is a conditional effect
			
			// Split up conditional effect and add each of them
			for (ConsequentialCondition cc : split((ConsequentialCondition) op.getEffect())) {							
				effectSet.add(cc);
			}
		} else if (op.getEffect().getConditionType() == ConditionType.conjunction) {
			// Top level is a conjunction
			
			// For all contained effects:
			for (AbstractCondition c : ((ConditionSet) op.getEffect()).getConditions()) {
				
				if (c.getConditionType() == ConditionType.consequential) {
					// Split conditional effect
					for (ConsequentialCondition cc : split((ConsequentialCondition) c)) {							
						effectSet.add(cc);
					}
				} else {
					// Add usual effect
					effectSet.add(c);
				}
			}
		} else {
			// Top level is atomic
			effect = op.getEffect();
		}
		
		// Split operator along its top-level disjunction
		if (op.getPrecondition().getConditionType() == ConditionType.disjunction) {
			
			// Create new operator for each contained element in disjunction
			int counter = 1;
			for (AbstractCondition child : ((ConditionSet) op.getPrecondition()).getConditions()) {
				
				Operator opSplit = new Operator(op.getName() + "$" + counter + "$");
				for (Argument arg : op.getArguments()) {
					opSplit.addArgument(arg);
				}
				opSplit.setPrecondition(child);
				opSplit.setEffect(effect);
				
				newOperators.add(opSplit);
				counter++;
			}
			
		} else {
			// No disjunction: Operator does not need to be split
			Operator opSplit = new Operator(op.getName());
			for (Argument arg : op.getArguments()) {
				opSplit.addArgument(arg);
			}
			opSplit.setPrecondition(op.getPrecondition().copy());
			opSplit.setEffect(effect);
			
			newOperators.add(opSplit);
		}
		
		return newOperators;
	}
	
	/**
	 * If the provided conditional effect (in DNF!) has a disjunction on its
	 * top level, it is split into multiple conditional effects which together
	 * are equivalent to the original one. If no disjunction is present,
	 * the original conditional effect is returned as the only element.
	 */
	private List<ConsequentialCondition> split(ConsequentialCondition cond) {
		
		List<ConsequentialCondition> splitConds = new ArrayList<>();
		if (cond.getPrerequisite().getConditionType() == ConditionType.disjunction) {
			// For each element in the disjunction, create a new cond. effect
			for (AbstractCondition partCond : ((ConditionSet) cond.getPrerequisite()).getConditions()) {
				ConsequentialCondition partCC = new ConsequentialCondition();
				partCC.setPrerequisite(partCond);
				partCC.setConsequence(cond.getConsequence());
				splitConds.add(partCC);
			}
		} else {
			// The prerequisite is already simple, so add the old cond. effect
			splitConds.add(cond);
		}
		
		return splitConds;
	}
	
	private void resolveDerivedPredicates() {
		
		// Resolve derived predicates in operator preconditions
		// and conditional effects
		for (Operator op : problem.getOperators()) {
			AbstractCondition cond = op.getPrecondition();
			op.setPrecondition(resolveDerivedPredicates(cond));
			cond = op.getEffect();
			op.setEffect(resolveDerivedPredicates(cond));
		}
		
		// Resolve derived predicates in goals
		List<AbstractCondition> newGoals = new ArrayList<>();
		for (AbstractCondition goal : problem.getGoals()) {
			newGoals.add(resolveDerivedPredicates(goal));
		}
		problem.getGoals().clear();
		problem.getGoals().addAll(newGoals);
	}
	
	private AbstractCondition resolveDerivedPredicates(AbstractCondition cond) {
		
		switch (cond.getConditionType()) {
		
		// All cases besides atomic: just propagating down
		case atomic:
			if (cond instanceof DerivedCondition) {
				// Return the condition which is inside the derived predicate
				DerivedPredicate p = ((DerivedCondition) cond).getPredicate();
				AbstractCondition innerCondition = ((DerivedPredicate) 
					((Condition) cond).getPredicate()
				).getCondition();
				innerCondition = innerCondition.getConditionBoundToArguments(
						p.getArguments(), ((Condition) cond).getArguments());
				// There may be nested derived predicates
				return resolveDerivedPredicates(innerCondition);
			} else {				
				return cond.copy();
			}
		case negation:
			Negation n = new Negation();
			n.setChildCondition(resolveDerivedPredicates(
					((Negation) cond).getChildCondition()));
			return n;
		case conjunction:
		case disjunction:
			ConditionSet set = new ConditionSet(cond.getConditionType());
			for (AbstractCondition c : ((ConditionSet) cond).getConditions()) {
				set.add(resolveDerivedPredicates(c));
			}
			return set;
		case consequential:
			ConsequentialCondition condCC = (ConsequentialCondition) cond;
			ConsequentialCondition cc = new ConsequentialCondition();
			cc.setPrerequisite(resolveDerivedPredicates(condCC.getPrerequisite()));
			cc.setConsequence(resolveDerivedPredicates(condCC.getConsequence()));
			return cc;
		case implication:
			Implication condImp = (Implication) cond;
			Implication i = new Implication();
			i.setIfCondition(resolveDerivedPredicates(condImp.getIfCondition()));
			i.setThenCondition(resolveDerivedPredicates(condImp.getThenCondition()));
			return i;
			
		case quantification:
			Quantification q = (Quantification) cond;
			Quantification qNew = new Quantification(q.getQuantifier());
			for (Argument arg : q.getVariables()) {
				qNew.addVariable(arg);
			}
			qNew.setCondition(resolveDerivedPredicates(q.getCondition()));
			return qNew;
			
		default:
			return null;
		}
	}
}

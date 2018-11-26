package edu.kit.aquaplanning.grounding;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.Atom;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.model.lifted.AbstractCondition;
import edu.kit.aquaplanning.model.lifted.Operator;
import edu.kit.aquaplanning.model.lifted.PlanningProblem;
import edu.kit.aquaplanning.model.lifted.Quantification;

/**
 * Grounder doing a reachability analysis through some 
 * approximated state space until a fixpoint is reached.
 */
public class RelaxedPlanningGraphGrounder extends BaseGrounder {
	
	/**
	 * Grounds the entire problem.
	 */
	@Override
	public GroundPlanningProblem ground(PlanningProblem problem) {
		
		this.problem = problem;
		
		// Create a sorted list of constants
		constants = new ArrayList<>();
		constants.addAll(problem.getConstants());
		constants.sort((c1, c2) -> c1.getName().compareTo(c2.getName()));
				
		// Traverse delete-relaxed state space
		RelaxedPlanningGraph graph = new RelaxedPlanningGraph(problem);
		actions = new ArrayList<>();
		int iteration = 0;
		while (graph.hasNextLayer()) {
			graph.computeNextLayer();
			// Ground new operators
			for (Operator op : graph.getLiftedActions(iteration)) {
				Action a = getAction(op); // actual grounding
				if (!actions.contains(a)) {
					actions.add(a);
				}
			}
			iteration++;
		}
		
		// Extract initial state
		List<Atom> initialStateAtoms = new ArrayList<>();
		graph.getLiftedState(0).forEach(cond -> {
			initialStateAtoms.add(atom(cond.getPredicate(), cond.getArguments()));
		});
		State initialState = new State(initialStateAtoms);
		
		// Extract goal
		List<Atom> goalAtoms = new ArrayList<>();
		problem.getGoals().forEach(cond -> {
			Atom atom = atom(cond.getPredicate(), cond.getArguments());
			atom.set(!cond.isNegated());
			goalAtoms.add(atom);
		});
		for (Quantification q : problem.getQuantifiedGoals()) {
			// Resolve quantifications into flat sets of atoms
			List<AbstractCondition> conditions = ArgumentCombination.resolveQuantification(
					q, problem, constants);
			goalAtoms.addAll(getAtoms(conditions));
		}
		Goal goal = new Goal(goalAtoms);
		
		// Assemble finished problem
		GroundPlanningProblem planningProblem = new GroundPlanningProblem(initialState, actions, 
				goal, problem.hasActionCosts(), extractAtomNames());
		return planningProblem;
	}
}

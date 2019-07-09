package edu.kit.aquaplanning.planning.cube.heuristic;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.ground.Atom;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.planning.cube.datastructure.ForwardSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;

public abstract class GenericHeuristic {

	public static GenericHeuristic getHeuristic(Configuration config) {
		switch (config.cubeFindHeuristic) {
		case relaxedPathLength:
			return new GenericRelaxedPathLength();
		case manhattanGoalDistance:
			return new GenericManhattanGoalDistance();
		case ffTrautmann:
			return new GenericTrautmann();
		case ffFroleyks:
			return new GenericFroleyks();
		case ffWilliams:
			return new GenericWilliams();
		default:
			break;
		}
		return null;
	}
	
	public abstract int value(GenericSearchNode node);
	
	public int stateDistance(State state1, State state2, GroundPlanningProblem problem) {
		GroundPlanningProblem tempProblem = new GroundPlanningProblem(problem);
		tempProblem.setInitialState(state1);
		tempProblem.setGoal(makeStateToGoal(problem, state2));
		return value(new ForwardSearchNode(tempProblem));
	}
	
	private Goal makeStateToGoal(GroundPlanningProblem problem, State state) {
		List<Atom> atomList = new ArrayList<Atom>();
		List<Boolean> stateList = state.getAtoms();

		for (int i = 0; i < problem.getAtomNames().size(); i++) {
			if (stateList.get(i)) {
				atomList.add(new Atom(i, problem.getAtomNames().get(i), true));
			} else {
				atomList.add(new Atom(i, problem.getAtomNames().get(i), false));
			}
		}
		return new Goal(atomList);
	}
}

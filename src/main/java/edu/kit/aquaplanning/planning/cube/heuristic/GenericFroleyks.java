package edu.kit.aquaplanning.planning.cube.heuristic;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;
import edu.kit.aquaplanning.planning.datastructures.GroundRelaxedPlanningGraph;

public class GenericFroleyks extends GenericHeuristic{

	@Override
	public int value(GenericSearchNode node) {
		State state = node.getState();
		Goal goal = node.getGoal();
		GroundPlanningProblem problem = node.problem;

		// Is the goal already satisfied (in a relaxed definition)?
		if (goal.isSatisfiedRelaxed(state)) {
			return 0;
		}

		// Traverse deletion-relaxed planning graph
		List<State> states = new ArrayList<>();
		states.add(state);
		GroundRelaxedPlanningGraph graph = new GroundRelaxedPlanningGraph(problem, state, problem.getActions());
		State g_hat = new State(goal.getAtoms());
		while (graph.hasNextLayer()) {
			State nextState = graph.computeNextLayer();
			states.add(nextState);
			// Goal reached?
			if (nextState.isSupersetOf(g_hat)) {
				List<Action> allActions = problem.getActions();
				Plan p = new Plan();
				for (int stateIndex = states.size() - 2; stateIndex > 0; --stateIndex) {
					State s_hat = states.get(stateIndex);
					List<Action> A = new ArrayList<>();

					// Choose set of actions − with a sledgehammer
					for (Action action : allActions) {
						if (s_hat.isSupersetOf(g_hat)) {
							break;
						}
						if (action.isApplicableRelaxed(s_hat)) {
							if (!s_hat.holdsAll(action.getEffectsPos())) {
								A.add(action);
								action.applyRelaxed(s_hat);
							}
						}
					}
					for (Action action : A) {
						p.appendAtFront(action);
						g_hat.removeAll(action.getEffectsPos());
					}

					for (Action action : A) {
						g_hat.addAll(action.getPreconditionsPos());
					}
				}
				return p.getLength();
			}
		}

		// Goals could not be reached: unsolvable from this state
      return Integer.MAX_VALUE;
	}
}

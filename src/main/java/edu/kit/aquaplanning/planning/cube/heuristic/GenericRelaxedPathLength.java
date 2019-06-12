package edu.kit.aquaplanning.planning.cube.heuristic;

import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;
import edu.kit.aquaplanning.planning.datastructures.GroundRelaxedPlanningGraph;

public class GenericRelaxedPathLength extends GenericHeuristic{
	
	public GenericRelaxedPathLength() {
		super();
	}
	
	@Override
	public int value(GenericSearchNode node) {
		
		State state = node.getState();
		Goal goal = node.getGoal();
		
		// Is the goal already satisfied (in a relaxed definition)?
		if (goal.isSatisfiedRelaxed(state)) {
			return 0;
		}
		
		// Traverse deletion-relaxed planning graph
		GroundRelaxedPlanningGraph graph = new GroundRelaxedPlanningGraph(node.problem, state, node.problem.getActions());
		int depth = 1; 
		while (graph.hasNextLayer()) {
			State nextState = graph.computeNextLayer();
			
			// Goal reached?
			if (goal.isSatisfiedRelaxed(nextState)) {
				return depth;
			}
			
			depth++;
		}
		
		// Goals could not be reached: unsolvable from this state
		return Integer.MAX_VALUE;
	}
}

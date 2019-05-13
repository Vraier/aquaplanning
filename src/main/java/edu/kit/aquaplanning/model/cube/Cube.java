package edu.kit.aquaplanning.model.cube;

import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.planning.datastructures.SearchNode;

//TODO: Think more about other possibilities of a cube. Think of a different interface.
public class Cube {

	private GroundPlanningProblem problem;
	private SearchNode node;

	public Cube(GroundPlanningProblem problem, SearchNode node) {

		GroundPlanningProblem newProblem = new GroundPlanningProblem(problem);
		newProblem.setInitialState(node.state);
		this.problem = newProblem;
		this.node = node;
	}

	public GroundPlanningProblem getProblem() {
		return problem;
	}

	public Plan extractPlan() {
		Plan plan = new Plan();
		while (node != null && node.lastAction != null) {
			plan.appendAtFront(node.lastAction);
			node = node.parent;
		}
		return plan;
	}
}

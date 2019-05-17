package edu.kit.aquaplanning.model.cube;

import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.ground.State;

public class Cube {

	private GroundPlanningProblem problem;
	private Plan partialPlanFront;
	private Plan partialPlanBack;

	public Cube(GroundPlanningProblem problem, State state, Plan plan) {

		GroundPlanningProblem newProblem = new GroundPlanningProblem(problem);
		newProblem.setInitialState(state);
		this.problem = newProblem;
		this.partialPlanFront = plan;
		this.partialPlanBack = new Plan();
	}
	
	public Cube(GroundPlanningProblem problem, Goal goal, Plan plan) {
		
		GroundPlanningProblem newProblem = new GroundPlanningProblem(problem);
		newProblem.setGoal(goal);
		this.problem = newProblem;
		this.partialPlanFront = new Plan();
		this.partialPlanBack = plan;
	}

	public GroundPlanningProblem getProblem() {
		return problem;
	}

	/**
	 * takes a partial Plan and concate it with the partial plan for the cube to get
	 * a valid solution for the original Problem.
	 */
	public Plan concatePlan(Plan plan) {
		plan.concateAtFront(partialPlanFront);
		plan.concateAtBack(partialPlanBack);
		return plan;
	}
}

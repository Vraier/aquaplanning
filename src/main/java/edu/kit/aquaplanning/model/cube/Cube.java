package edu.kit.aquaplanning.model.cube;

import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.ground.State;

public class Cube {

	private GroundPlanningProblem problem;
	private Plan partialPlanFront;
	private Plan partialPlanBack;

	/**
	 * Constructs a new Cube. In this case the cube represents a Problem with a new
	 * initial state. This type of cube results from a forward search.
	 * 
	 * @param problem The original problem
	 * @param state   the new initial state of the new problem
	 * @param plan    a partial Plan leading from the initial state of the original
	 *                problem to the new initial state
	 */
	public Cube(GroundPlanningProblem problem, State state, Plan plan) {

		GroundPlanningProblem newProblem = new GroundPlanningProblem(problem);
		newProblem.setInitialState(state);
		this.problem = newProblem;
		this.partialPlanFront = plan;
		this.partialPlanBack = new Plan();
	}

	/**
	 * Constructs a new Cube. In this case the cube represents a Problem with a new
	 * goal. This type of cube results from a backward search.
	 * 
	 * @param problem The original problem
	 * @param state   the new goal of the new problem
	 * @param plan    a partial Plan leading from the goal of the original problem
	 *                to the new goal
	 */
	public Cube(GroundPlanningProblem problem, Goal goal, Plan plan) {

		GroundPlanningProblem newProblem = new GroundPlanningProblem(problem);
		newProblem.setGoal(goal);
		this.problem = newProblem;
		this.partialPlanFront = new Plan();
		this.partialPlanBack = plan;
	}

	/**
	 * Returns the problem that this cube is representing
	 */
	public GroundPlanningProblem getProblem() {
		return problem;
	}

	/**
	 * Takes a partial Plan for the problem of the cube and concates it with another
	 * partial plan to get a valid solution for the original Problem.
	 */
	public void finalizePlan(Plan plan) {
		plan.concateAtFront(partialPlanFront);
		plan.concateAtBack(partialPlanBack);
	}
}

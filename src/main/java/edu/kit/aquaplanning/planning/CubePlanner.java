package edu.kit.aquaplanning.planning;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;

//TODO: fix implementation of the Planner interface. Maybe don't inherit from Planner? 
public abstract class CubePlanner extends Planner {

	protected Cube cube;

	public CubePlanner(Configuration config, Cube cube) {
		super(config);
		this.cube = cube;
	}

	/**
	 * Calculates the given amount of steps and tries to find a plan for the cube.
	 * If no plan is found in the given amount of steps null is returned. The next
	 * call on calculateSteps will continue with the progress of the last call.
	 */
	public abstract Plan calculateSteps(int steps);

	/**
	 * Returns true if the Planner exhausted his search space and could not find a
	 * plan.
	 */
	public abstract boolean isExhausted();
	
	public abstract int getTotalIterations();

	public Plan findPlan(GroundPlanningProblem problem) {
		Plan plan;
		do {
			plan = calculateSteps(Integer.MAX_VALUE);
		} while (plan == null && !this.isExhausted());
		return plan;
	}
}

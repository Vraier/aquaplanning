package edu.kit.aquaplanning.planning.cube;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.Plan;

public abstract class CubeSolver {

	// Variables that hold the state of the planner
	protected Cube cube;
	protected Configuration config;
	protected boolean isExhausted = false;
	protected int totalIterations = 0;
	protected long totalTime = 0;

	// Variable for checking computational Bounds
	protected long searchStartMillis = 0;
	protected int iterationLimit = 0;
	protected long timeLimit = 0;

	public CubeSolver(Configuration config, Cube cube) {
		this.config = config;
		this.cube = cube;
	}

	/**
	 * Tries to find a plan for its cube while staying in the computational bounds.
	 * If no plan is found in the given amount of steps or time null is returned.
	 * The next call on calculateSteps will continue with the progress of the last
	 * call.
	 */
	public abstract Plan calculateSteps();

	/**
	 * Returns true if the Planner exhausted his search space and could not find a
	 * plan.
	 */
	public boolean isExhausted() {
		return isExhausted;
	}

	/**
	 * Returns an approximation of the distance this Solver is away from its current
	 * goal. The solver should be provided with a heuristic to be able to calculate
	 * such an approximation.
	 * 
	 * @return an approximation of the distance to its goal.
	 */
	public abstract int getBestDistance();

	public int getTotalIterations() {
		return totalIterations;
	}

	public long getTotalTime() {
		return totalTime;
	}

	protected void startSearch() {
		searchStartMillis = System.currentTimeMillis();
	}

	/**
	 * Sets a limit for the time the solver should try to solve a cube within one
	 * call of calculateSteps()
	 * 
	 * @param milliSeconds
	 *            the amount of time to solve a cube
	 */
	public void setTimeLimit(long milliSeconds) {
		this.timeLimit = milliSeconds;
	}

	/**
	 * Sets a limit of the amount of iterations this solver should try to solve a
	 * cube within one call of calculateSteps()
	 * 
	 * @param iterations
	 *            the amount of steps to solve a cube
	 */
	public void setIterationLimit(int iterations) {
		this.iterationLimit = iterations;
	}

	/**
	 * Checks if we are in computational bounds. This means that our thread is not
	 * interrupted and we didn't exceed our time limit. The time limit is given by
	 * the configuration. There is no possibility to limit the cube finding by a
	 * given amount of iterations.
	 */
	protected boolean withinTimeLimit() {

		if (Thread.currentThread().isInterrupted())
			return false;

		if (config.maxTimeSeconds > 0) {
			long totalTime = System.currentTimeMillis() - config.startTimeMillis;
			if (totalTime > config.maxTimeSeconds * 1000) {
				return false;
			}
		}

		return true;
	}

	protected boolean withinComputationalBounds(int iterations) {

		if (iterationLimit > 0 && iterations >= iterationLimit) {
			return false;
		}

		if (timeLimit > 0) {
			long searchTime = System.currentTimeMillis() - searchStartMillis;
			if (searchTime > timeLimit) {
				return false;
			}
		}
		return true;
	}
}

package edu.kit.aquaplanning.planning;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.Plan;

public abstract class CubePlanner {

	// Variables that hold the state of the Plannner
	protected Cube cube;
	protected Configuration config;
	protected boolean isExhausted = false;
	protected int totalIterations = 0;

	// Variable for checking computational Bounds
	protected long searchStartMillis = 0;
	protected int iterationLimit = 0;
	protected long timeLimit = 0;

	public CubePlanner(Configuration config, Cube cube) {
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

	public int getTotalIterations() {
		return totalIterations;
	}

	protected void startSearch() {
		searchStartMillis = System.currentTimeMillis();
	}

	public void setTimeLimit(long milliSeconds) {
		this.timeLimit = milliSeconds;
	}

	public void setIterationLimit(int iterations) {
		this.iterationLimit = iterations;
	}

	/**
	 * Checks the used amount of iterations and the elapsed time against
	 * computational bounds specified by the setter methods. If false is returned,
	 * the planner should stop.
	 */
	protected boolean withinComputationalBounds(int iterations) {

		if (Thread.currentThread().isInterrupted())
			return false;

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

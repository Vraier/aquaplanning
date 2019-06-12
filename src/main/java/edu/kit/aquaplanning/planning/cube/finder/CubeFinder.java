package edu.kit.aquaplanning.planning.cube.finder;

import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;

public abstract class CubeFinder {

	// Variable for checking computational Bounds
	protected long searchStartMillis = 0;

	protected Configuration config;
	protected Plan plan = null;

	public CubeFinder(Configuration config) {
		this.config = config;
	}

	/**
	 * finds the specified amount of new cubes.
	 * 
	 * @param problem
	 *            the problem to search for new cubes
	 * @param numCubes
	 *            the number of cubes to search for
	 * @return a list containing all the cubes. Or null if we already found a
	 *         solution to the given problem while searching for cubes.
	 */
	public abstract List<Cube> findCubes(GroundPlanningProblem problem, int numCubes);

	/**
	 * This method should only be called if findCubes() returns null. In this case
	 * we already found a plan.
	 * 
	 * @return The plan found while searching for cubes.
	 */
	public Plan getPlan() {
		return plan;
	}

	/**
	 * Constructs a new cube finder specified by the given configuration.
	 * 
	 * @param config
	 *            the configuration describing the cube finder
	 * @return the new cube finder
	 */
	public static CubeFinder getCubeFinder(Configuration config) {
		switch (config.cubeFinderMode) {
		case forwardSearch:
			return new ForwardSearchCubeFinder(config);
		case backwardSearch:
			return new BackwardSearchCubeFinder(config);
		default:
			break;
		}
		return null;
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
}

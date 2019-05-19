package edu.kit.aquaplanning.planning.cube;

import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;

//TODO: add computational Bounds
//TODO: add cutoffHeuristic
public abstract class CubeFinder {

	Configuration config;
	Plan plan = null;

	public CubeFinder(Configuration config) {
		this.config = config;
	}

	/**
	 * finds the specified amount of new cubes.
	 * 
	 * @param problem  the problem to search for new cubes
	 * @param numCubes the number of cubes to search for
	 * @return a list containing all the cubes. Or null if we already found a
	 *         solution to the given problem while searching for cubes.
	 */
	public abstract List<Cube> findCubes(GroundPlanningProblem problem, int numCubes);
	
	// public abstract List<Cube> findCubes(GroundPlanningProblem problem, CutoffHeuristic heuristic);

	/**
	 * This method should only be called if findCubes() returns null. In this case
	 * we already found a plan.
	 * 
	 * @return The plan found while searching for cubes.
	 */
	public abstract Plan getPlan();

	/**
	 * Constructs a new cube finder specified by the given configuration.
	 * 
	 * @param config the configuration describing the cube finder
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
}

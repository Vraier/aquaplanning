package edu.kit.aquaplanning.planning.cube.finder;

import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchQueue;
import edu.kit.aquaplanning.util.Logger;

public abstract class GenericCubeFinder extends CubeFinder {

	protected GenericSearchQueue frontier;
	protected int totalIterations = 0;

	public GenericCubeFinder(Configuration config) {
		super(config);
	}

	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem, int numCubes) {

		initializeFrontier(problem);

		while (!frontier.isEmpty() && frontier.size() < numCubes && withinTimeLimit()) {

			GenericSearchNode node = frontier.get();

			if (node.satisfiesProblem()) {
				Logger.log(Logger.INFO,
						"Generic Cube Finder already found a plan after " + totalIterations + " steps.");
				plan = node.getPartialPlan();
				return null;
			}

			for (GenericSearchNode child : node.getPredecessors()) {
				frontier.add(child);
			}

			totalIterations++;

		}
		assert(frontier.size() > 0);
		Logger.log(Logger.INFO, "Generic Cube Finder stopped search after " + totalIterations + " steps.");
		Logger.log(Logger.INFO, "Generic Cube Finder found " + frontier.size() + " cubes.");

		return frontier.getCubes();
	}

	/**
	 * Should be overridden in child classes. Must construct the frontier and fill
	 * it with the starting node. A Backward Search will start witch a backward node
	 * and a forward search with a forward node.
	 * 
	 * @param problem
	 *            the problem to search for cubes
	 */
	protected abstract void initializeFrontier(GroundPlanningProblem problem);

}

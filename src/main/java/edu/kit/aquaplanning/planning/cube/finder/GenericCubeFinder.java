package edu.kit.aquaplanning.planning.cube.finder;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchQueue;
import edu.kit.aquaplanning.util.Logger;

public abstract class GenericCubeFinder extends CubeFinder {

	// Frontier gets initialized by the call of initializeFrontierWithNode()
	protected GenericSearchQueue frontier;
	protected int totalIterations = 0;

	public int totalFrontierSize = 0;
	public int totalAnchorSize = 0;
	public int totalCutOffSize = 0;

	public GenericCubeFinder(Configuration config) {
		super(config);
	}

	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem, int numCubes) {

		initializeFrontierWithNode(problem);

		while (!frontier.isEmpty() && frontier.size() < numCubes && withinTimeLimit()) {

			totalIterations++;

			GenericSearchNode node = frontier.get();
			
			if(node == null) {
				assert(frontier.isEmpty());
				continue;
			}

			if (node.satisfiesProblem()) {
				Logger.log(Logger.INFO,
						"Generic Cube Finder already found a plan after " + totalIterations + " steps.");
				plan = node.getPartialPlan();
				
				totalFrontierSize = frontier.size();
				totalAnchorSize = frontier.anchorSize();
				totalCutOffSize = frontier.cutOffSize();
				return null;
			}

			for (GenericSearchNode child : node.getPredecessors()) {
				frontier.add(child);
			}
		}
		if (!withinTimeLimit()) {
			// We return an empty list so signalize that we should stop searching for a
			// solution
			return new ArrayList<Cube>();
		}

		totalFrontierSize = frontier.size();
		totalAnchorSize = frontier.anchorSize();
		totalCutOffSize = frontier.cutOffSize();

		return frontier.getCubes();
	}

	@Override
	public void logInformation() {
		if (!withinTimeLimit()) {
			Logger.log(Logger.INFO, "Generic Cube Finder exceeded his time limit or got interrupted.");
		}
		if (frontier.isEmpty()) {
			Logger.log(Logger.INFO,
					"Generic Cube Finder emptyed his search queue and found no plan. The Problem has no solution.");
		}

		Logger.log(Logger.INFO, "Generic Cube Finder stopped search after " + totalIterations + " steps.");
		Logger.log(Logger.INFO, "Generic Cube Finder found " + totalFrontierSize + " cubes from which "
				+ totalCutOffSize + " were cut off and " + totalAnchorSize + " were anchors.");
	}

	/**
	 * Should be overridden in child classes. Must construct the frontier and fill
	 * it with the starting node. A Backward Search will start witch a backward node
	 * and a forward search with a forward node.
	 * 
	 * @param problem
	 *            the problem to search for cubes
	 */
	protected abstract void initializeFrontierWithNode(GroundPlanningProblem problem);
}

package edu.kit.aquaplanning.planning.cube.finder;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.planning.cube.datastructure.ForwardSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchQueue;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericHeuristic;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy;

public class ForwardSearchCubeFinder extends GenericCubeFinder {

	public ForwardSearchCubeFinder(Configuration config) {
		super(config);
	}

	@Override
	protected void initializeFrontierWithNode(GroundPlanningProblem problem) {

		Configuration tempConfig = config.copy();
		tempConfig.searchStrategy = config.cubeFindSearchStrategy;
		SearchStrategy strategy = new SearchStrategy(tempConfig);

		GenericHeuristic heuristic = GenericHeuristic.getHeuristic(config);
		frontier = new GenericSearchQueue(strategy, heuristic);
		ForwardSearchNode initalNode = new ForwardSearchNode(problem);
		frontier.add(initalNode);
	}
}

package edu.kit.aquaplanning.planning.cube.finder;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.planning.cube.cutoffHeuristic.CutOffHeuristic;
import edu.kit.aquaplanning.planning.cube.datastructure.BBackwardSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchQueue;
import edu.kit.aquaplanning.planning.cube.heuristic.GenericHeuristic;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy;

public class BBackwardSearchCubeFinder extends GenericCubeFinder {

	public BBackwardSearchCubeFinder(Configuration config) {
		super(config);
	}

	@Override
	protected void initializeFrontier(GroundPlanningProblem problem) {
		Configuration tempConfig = config.copy();
		tempConfig.searchStrategy = config.cubeFindSearchStrategy;
		SearchStrategy strategy = new SearchStrategy(tempConfig);
		
		GenericHeuristic heuristic = GenericHeuristic.getHeuristic(config);
		CutOffHeuristic cutOffHeuristic = CutOffHeuristic.getCutOffHeuristic(config);
		frontier = new GenericSearchQueue(strategy, heuristic, cutOffHeuristic);
		frontier.add(new BBackwardSearchNode(problem));
	}
}

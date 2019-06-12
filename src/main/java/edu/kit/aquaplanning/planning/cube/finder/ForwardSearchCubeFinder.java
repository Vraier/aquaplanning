package edu.kit.aquaplanning.planning.cube.finder;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.planning.datastructures.ActionIndex;
import edu.kit.aquaplanning.planning.datastructures.SearchNode;
import edu.kit.aquaplanning.planning.datastructures.SearchQueue;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy;
import edu.kit.aquaplanning.planning.heuristic.Heuristic;

public class ForwardSearchCubeFinder extends CubeFinder {

	public ForwardSearchCubeFinder(Configuration config) {
		super(config);

		// update our configuration for easier usage while creating the forward search
		// datastructures
		Configuration newConfig = config.copy();
		newConfig.searchStrategy = config.cubeFindSearchStrategy;
		newConfig.heuristic = config.cubeFindHeuristic;
		newConfig.heuristicWeight = config.cubeFindHeuristicWeight;
		this.config = newConfig;
	}

	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem, int numCubes) {

		List<Cube> cubes;
		State initState = problem.getInitialState();
		Goal goal = problem.getGoal();
		ActionIndex aindex = new ActionIndex(problem);

		// Initialize forward search
		SearchQueue frontier;
		SearchStrategy strategy = new SearchStrategy(config);
		if (strategy.isHeuristical()) {
			Heuristic heuristic = Heuristic.getHeuristic(problem, config);
			frontier = new SearchQueue(strategy, heuristic);
		} else {
			frontier = new SearchQueue(strategy);
		}
		frontier.add(new SearchNode(null, initState));

		while (!frontier.isEmpty() && frontier.size() < numCubes) {

			SearchNode node = frontier.get();

			// Is the goal reached?
			if (goal.isSatisfied(node.state)) {

				// Extract plan
				plan = new Plan();
				while (node != null && node.lastAction != null) {
					plan.appendAtFront(node.lastAction);
					node = node.parent;
				}
				// no cubes to return because we already found a plan
				return null;
			}

			// Expand node: iterate over operators
			for (Action action : aindex.getApplicableActions(node.state)) {
				// Create new node by applying the operator
				State newState = action.apply(node.state);

				// Add new node to frontier
				SearchNode newNode = new SearchNode(node, newState);
				newNode.lastAction = action;
				frontier.add(newNode);
			}
		}

		// retrieve all nodes from the queue
		cubes = new ArrayList<Cube>();
		while (!frontier.isEmpty()) {
			SearchNode node = frontier.get();
			cubes.add(new Cube(problem, node.state, node.getPartialPlan()));
		}
		return cubes;
	}

	@Override
	public Plan getPlan() {
		return plan;
	}
}

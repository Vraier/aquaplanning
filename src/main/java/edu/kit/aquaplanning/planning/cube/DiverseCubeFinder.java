package edu.kit.aquaplanning.planning.cube;

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

public class DiverseCubeFinder extends CubeFinder {

	public DiverseCubeFinder(Configuration config) {
		super(config);

		// update our configuration for easier usage while creating the forward search
		// data structures
		Configuration newConfig = config.copy();
		newConfig.searchStrategy = config.cubeFindSearchStrategy;
		newConfig.heuristic = config.cubeFindHeuristic;
		newConfig.heuristicWeight = config.cubeFindHeuristicWeight;
		this.config = newConfig;
	}

	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem, int numCubes) {

		State initState = problem.getInitialState();
		Goal goal = problem.getGoal();
		ActionIndex aindex = new ActionIndex(problem);

		// Initialize forward search
		List<Cube> cubes;
		List<SearchNode> anchors = new ArrayList<SearchNode>();
		List<SearchNode> cutOffs = new ArrayList<SearchNode>();
		SearchQueue frontier;
		SearchStrategy strategy = new SearchStrategy(config);
		StateDistanceHeuristic cutOffHeuristic = new ManhattanDistanceHeuristic(13);
		
		if (strategy.isHeuristical()) {
			Heuristic heuristic = Heuristic.getHeuristic(problem, config);
			frontier = new SearchQueue(strategy, heuristic);
		} else {
			frontier = new SearchQueue(strategy);
		}
		
		frontier.add(new SearchNode(null, initState));

		while (!frontier.isEmpty() && cutOffs.size() < numCubes) {

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

			// Check if we are too close to an already found node
			boolean cutOff = false;
			for (SearchNode a : anchors) {
				if(cutOffHeuristic.cutOff(a.state, node.state)) {
					cutOff = true;
					break;
				}
			}
			
			// Node should be cut off because we are too close to an anchor
			if(cutOff) {
				cutOffs.add(node);
				break;
			}
			
			// Node is deep enough. We can cut it off and use it as an anchor
			if(node.depth > 25) {
				anchors.add(node);
				cutOffs.add(node);
				break;
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

		// retrieve all cut off nodes
		cubes = new ArrayList<Cube>();
		for(SearchNode node: cutOffs) {
			cubes.add(new Cube(problem, node.state, node.getPartialPlan()));
		}
		return cubes;
	}

	@Override
	public Plan getPlan() {
		return plan;
	}
}

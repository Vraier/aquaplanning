package edu.kit.aquaplanning.planning.cube;

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
import edu.kit.aquaplanning.util.Logger;

public class ForwardSearchCubeSolver extends CubeSolver {

	private GroundPlanningProblem problem;
	private State state;
	private Goal goal;
	private ActionIndex aindex;
	private SearchStrategy strategy;
	private SearchQueue frontier;

	public ForwardSearchCubeSolver(Configuration config, Cube cube) {

		super(config, cube);

		// Create new Configuration to use it with the already existing Planner Classes
		Configuration newConfig = config.copy();
		newConfig.searchStrategy = config.cubeSolveSearchStrategy;
		newConfig.heuristic = config.cubeSolveHeuristic;
		newConfig.heuristicWeight = config.cubeSolveHeuristicWeight;
		this.config = newConfig;

		problem = cube.getProblem();
		state = new State(problem.getInitialState());
		goal = problem.getGoal();
		aindex = new ActionIndex(problem);

		strategy = new SearchStrategy(this.config);
		if (strategy.isHeuristical()) {
			Heuristic heuristic = Heuristic.getHeuristic(problem, this.config);
			frontier = new SearchQueue(strategy, heuristic);
		} else {
			frontier = new SearchQueue(strategy);
		}
		frontier.add(new SearchNode(null, state));
	}

	@Override
	public Plan calculateSteps() {

		int iterations = 0;
		startSearch();

		while (withinTimeLimit() && withinComputationalBounds(iterations) && !frontier.isEmpty()) {

			SearchNode node = frontier.get();

			// Is the goal reached?
			if (goal.isSatisfied(node.state)) {

				// Extract plan
				Plan plan = new Plan();
				while (node != null && node.lastAction != null) {
					plan.appendAtFront(node.lastAction);
					node = node.parent;
				}
				totalIterations += iterations;
				totalTime += System.currentTimeMillis() - searchStartMillis;
				Logger.log(Logger.INFO, "ForwardSearchPlanner found a plan after " + totalIterations + " steps in "
						+ totalTime + " millisecs.");
				cube.finalizePlan(plan);
				return plan;
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
			iterations++;
		}
		// no plan exists
		if (frontier.isEmpty()) {
			isExhausted = true;
		}

		totalIterations += iterations;
		totalTime += System.currentTimeMillis() - searchStartMillis;
		return null;
	}
}

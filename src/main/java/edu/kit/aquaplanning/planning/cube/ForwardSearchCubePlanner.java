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

public class ForwardSearchCubePlanner extends CubePlanner {

	private GroundPlanningProblem problem;
	private State state;
	private Goal goal;
	private ActionIndex aindex;
	private SearchStrategy strategy;
	private SearchQueue frontier;

	public ForwardSearchCubePlanner(Configuration config, Cube cube) {

		super(config, cube);
		problem = cube.getProblem();
		state = new State(problem.getInitialState());
		goal = problem.getGoal();
		aindex = new ActionIndex(problem);		

		strategy = new SearchStrategy(config);
		if (strategy.isHeuristical()) {
			Heuristic heuristic = Heuristic.getHeuristic(problem, config);
			frontier = new SearchQueue(strategy, heuristic);
		} else {
			frontier = new SearchQueue(strategy);
		}
		frontier.add(new SearchNode(null, state));
	}

	@Override
	public Plan calculateSteps() {

		startSearch();
		int i = 0;

		while (withinComputationalBounds(i) && !frontier.isEmpty()) {

			SearchNode node = frontier.get();

			// Is the goal reached?
			if (goal.isSatisfied(node.state)) {

				// Extract plan
				Plan plan = new Plan();
				while (node != null && node.lastAction != null) {
					plan.appendAtFront(node.lastAction);
					node = node.parent;
				}
				totalIterations += i;
				totalTime += System.currentTimeMillis() - searchStartMillis;
				System.out.printf("ForwardSearchPlanner found a plan after %d steps in %d millisecs.\n",
						totalIterations, totalTime);
				return cube.concatePlan(plan);
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
			i++;
		}
		// no plan exists
		if (frontier.isEmpty()) {
			isExhausted = true;
		}

		totalIterations += i;
		totalTime += System.currentTimeMillis() - searchStartMillis;
		System.out.printf("ForwardSearchPlanner calculated %d steps in %d millisecs.\n", i,
				System.currentTimeMillis() - searchStartMillis);
		return null;
	}
}

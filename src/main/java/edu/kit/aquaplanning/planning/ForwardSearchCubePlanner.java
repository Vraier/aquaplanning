package edu.kit.aquaplanning.planning;

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

// TODO: implement possibility to use the config file
// TODO: support computationalBounds()
public class ForwardSearchCubePlanner extends CubePlanner {

	private GroundPlanningProblem problem;
	private State state;
	private Goal goal;
	private ActionIndex aindex;
	private SearchStrategy strategy;
	private SearchQueue frontier;

	private int iterations;
	private boolean isExhausted;

	public ForwardSearchCubePlanner(Configuration config, Cube cube) {

		super(config, cube);
		problem = cube.getProblem();
		state = new State(problem.getInitialState());
		goal = problem.getGoal();
		aindex = new ActionIndex(problem);

		strategy = new SearchStrategy(SearchStrategy.Mode.depthFirst);
		frontier = new SearchQueue(strategy);
		frontier.add(new SearchNode(null, state));

		/*
		 * strategy = new SearchStrategy(config); if (strategy.isHeuristical()) {
		 * Heuristic heuristic = Heuristic.getHeuristic(problem, config); frontier = new
		 * SearchQueue(strategy, heuristic); } else { frontier = new
		 * SearchQueue(strategy); }
		 */

		iterations = 0;
		isExhausted = false;
	}

	@Override
	public Plan calculateSteps(int steps) {

		int i = 0;
		while (i < steps && !Thread.currentThread().isInterrupted() && !frontier.isEmpty()) {

			i++;
			iterations++;
			SearchNode node = frontier.get();

			// Is the goal reached?
			if (goal.isSatisfied(node.state)) {

				// Extract plan
				Plan cubePlan = cube.extractPlan();
				Plan plan = new Plan();
				while (node != null && node.lastAction != null) {
					plan.appendAtFront(node.lastAction);
					node = node.parent;
				}
				cubePlan.concateAtBack(plan);
				return cubePlan;
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
		if (Thread.currentThread().isInterrupted()) {
			return null;
		}
		// no plan exists
		if (frontier.isEmpty()) {
			isExhausted = true;
		}

		// System.out.printf("Calculated %d steps\n", i);
		return null;
	}

	@Override
	public boolean isExhausted() {
		return isExhausted;
	}

	@Override
	public int getTotalIterations() {
		return iterations;
	}
}

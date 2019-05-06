package edu.kit.aquaplanning.planning;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.kit.aquaplanning.Configuration;
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

public class SimpelParallelPlanner extends Planner {

	private static final int NUM_CUBES = 10000;
	private static final int CUBE_ITERATIONS = 5000;
	private int numThreads;
	private List<Thread> threads;
	private Plan plan;

	private List<SearchNode> cubes;
	private int iteration;

	public SimpelParallelPlanner(Configuration config) {
		super(config);
		numThreads = config.numThreads;
	}

	@Override
	public Plan findPlan(GroundPlanningProblem problem) {
		startSearch();

		iteration = 0;
		threads = new ArrayList<>();
		plan = null;
		Random random = new Random(this.config.seed); // seed generator

		Logger.log(Logger.INFO, "Starting to search for " + NUM_CUBES + " cubes.");
		cubes = findCubes(problem, NUM_CUBES);
		Logger.log(Logger.INFO, "Found " + cubes.size() + " cubes.");

		if (plan != null) {
			Logger.log(Logger.INFO, "Already found a plan while searching for cubes.");
			return plan;
		} else if (cubes == null) {
			Logger.log(Logger.INFO, "Error occured while searching for cubes. Unable to find any cubes.");
			return null;
		}

		java.util.Collections.shuffle(cubes, random);

		for (int i = 0; i < numThreads; i++) {

			// Default configuration with random seed
			Configuration config = this.config.copy();
			config.seed = random.nextInt();
			int threadNum = i;

			// partition the cubes into equal parts
			// Round up integer division
			int partitionSize = ((cubes.size() + numThreads - 1) / numThreads);
			List<SearchNode> localCubes = cubes.subList(partitionSize * i,
					Math.min(partitionSize * (i + 1), cubes.size()));

			Thread thread = new Thread(new MyThread(config, threadNum, problem, localCubes));
			threads.add(thread);

			// Start the planner (non-blocking call)
			thread.start();
		}

		// Wait for all threads to finish
		// (if some plan has been found, all threads are interrupted)
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Plan is not null iff any planner was successful
		return plan;
	}

	/**
	 * Callback for when some planner finds a plan.
	 */
	private synchronized void onPlanFound(Plan plan) {
		if (this.plan != null) {
			// Another planner already found a plan
			return;
		}
		this.plan = plan;
		// Interrupt all planners
		for (Thread thread : threads) {
			// This interruption is acknowledged inside each planner thread
			// when withinComputationalBounds() is checked the next time.
			thread.interrupt();
		}
	}

	private List<SearchNode> findCubes(GroundPlanningProblem problem, int numCubes) {

		State initState = problem.getInitialState();
		Goal goal = problem.getGoal();
		ActionIndex aindex = new ActionIndex(problem);
		List<SearchNode> cubes;

		// Initialize breadthFirst search
		// TODO: implement own breadthFirstSerach for more controll over datastructures
		SearchStrategy strategy = new SearchStrategy(SearchStrategy.Mode.breadthFirst);
		SearchQueue frontier = new SearchQueue(strategy);
		frontier.add(new SearchNode(null, initState));

		while (withinComputationalBounds(iteration) && !frontier.isEmpty() && frontier.size() < numCubes) {

			// Visit node (by the heuristic provided to the priority queue)
			SearchNode node = frontier.get();

			// Is the goal reached?
			if (goal.isSatisfied(node.state)) {

				// Extract plan
				Plan plan = new Plan();
				while (node != null && node.lastAction != null) {
					plan.appendAtFront(node.lastAction);
					node = node.parent;
				}
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

			iteration++;
		}

		if (frontier.isEmpty()) {
			return new ArrayList<SearchNode>();
		} else if (!withinComputationalBounds(iteration)) {
			return null;
		} else {

			// retrieve all nodes from the queue
			cubes = new ArrayList<SearchNode>();
			while (!frontier.isEmpty()) {
				cubes.add(frontier.get());
			}
			return cubes;
		}
	}

	private class MyThread implements Runnable {

		// TODO: implement means to interrupt Threads
		Configuration config;
		int threadNum;
		GroundPlanningProblem problem;
		List<SearchNode> localCubes;

		public MyThread(Configuration config, int threadNum, GroundPlanningProblem problem, List<SearchNode> cubes) {
			this.config = config;
			this.threadNum = threadNum;
			this.problem = problem;
			this.localCubes = cubes;
		}

		@Override
		public void run() {

			// Create planners
			List<SimplePlanner> planners = new ArrayList<SimplePlanner>();
			for (SearchNode cube : localCubes) {
				State tempState = cube.state;
				GroundPlanningProblem tempProblem = new GroundPlanningProblem(problem);
				tempProblem.setInitialState(tempState);
				planners.add(new SimplePlanner(config, tempProblem));
			}

			Plan localPlan = null;
			int numExhausted = 0;
			for (int i = 0;; i = (i + 1) % planners.size()) {
				if (numExhausted >= planners.size()) {
					// no Planner found a Plan
					break;
				} else if (planners.get(i).isExhausted()) {
					numExhausted++;
					continue;
				} else {
					numExhausted = 0;
					localPlan = planners.get(i).calculateSteps(CUBE_ITERATIONS);
					if (localPlan != null) {
						Logger.log(Logger.INFO, "SimplePlanner \" (index " + threadNum + ") found a plan.");
						onPlanFound(localPlan);
					}
				}
			}
		}
	}

	private class SimplePlanner {

		private boolean isExhausted;
		private State initState;
		private Goal goal;
		private ActionIndex aindex;

		private SearchQueue frontier;
		private SearchStrategy strategy;

		public SimplePlanner(Configuration config, GroundPlanningProblem problem) {
			isExhausted = false;
			initState = problem.getInitialState();
			goal = problem.getGoal();
			aindex = new ActionIndex(problem);

			// Initialize forward search
			strategy = new SearchStrategy(config);
			if (strategy.isHeuristical()) {
				Heuristic heuristic = Heuristic.getHeuristic(problem, config);
				frontier = new SearchQueue(strategy, heuristic);
			} else {
				frontier = new SearchQueue(strategy);
			}
			frontier.add(new SearchNode(null, initState));
		}

		public Plan calculateSteps(int steps) {

			int i = 0;
			while (i < steps && !frontier.isEmpty()) {

				// Visit node (by the heuristic provided to the priority queue)
				SearchNode node = frontier.get();

				// Is the goal reached?
				if (goal.isSatisfied(node.state)) {

					// Extract plan
					Plan plan = new Plan();
					while (node != null && node.lastAction != null) {
						plan.appendAtFront(node.lastAction);
						node = node.parent;
					}
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
			}
			if (frontier.isEmpty()) {
				Logger.log(Logger.INFO, "Search space exhausted.");
				isExhausted = true;
			}
			return null;
		}

		public boolean isExhausted() {
			return isExhausted;
		}
	}
}

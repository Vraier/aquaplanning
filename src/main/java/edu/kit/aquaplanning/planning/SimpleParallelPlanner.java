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
import edu.kit.aquaplanning.util.Logger;

public class SimpleParallelPlanner extends Planner {

	private static final int NUM_CUBES = 12;
	private static final int CUBE_ITERATIONS = 5000;
	private int numThreads;
	private List<Thread> threads;
	private Plan plan;
	private int iteration;

	public SimpleParallelPlanner(Configuration config) {
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
		List<SearchNode> cubes;

		Logger.log(Logger.INFO, "Starting to search for " + NUM_CUBES + " cubes.");
		// System.out.println("Starting to search for " + NUM_CUBES + " cubes");
		cubes = findCubes(problem, NUM_CUBES);

		if (plan != null) {
			Logger.log(Logger.INFO, "Already found a plan while searching for cubes.");
			// System.out.println("Already found a plan while searching for cubes.");
			return plan;
		} else if (cubes.size() == 0) {
			Logger.log(Logger.INFO, "Unable to find any cubes. Problem has no solution.");
			// System.out.println("Unable to find any cubes. Problem has no solution.");
			return null;
		}
		Logger.log(Logger.INFO, "Found " + cubes.size() + " cubes.");
		// System.out.println("Found " + cubes.size() + " cubes");

		/*
		 * for(SearchNode cube: cubes) { System.out.println(cube.state); }
		 */

		java.util.Collections.shuffle(cubes, random);

		for (int i = 0; i < numThreads; i++) {

			// Default configuration with random seed
			Configuration config = this.config.copy();
			config.seed = random.nextInt();
			int threadNum = i + 1;

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
		SearchStrategy strategy = new SearchStrategy(SearchStrategy.Mode.breadthFirst);
		SearchQueue frontier = new SearchQueue(strategy);
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

			iteration++;
		}

		// retrieve all nodes from the queue
		cubes = new ArrayList<SearchNode>();
		while (!frontier.isEmpty()) {
			cubes.add(frontier.get());
		}
		return cubes;
	}

	private class MyThread implements Runnable {

		private Configuration config;
		private int threadNum;
		private GroundPlanningProblem baseProblem;
		private List<SearchNode> localCubes;
		private Plan localPlan;

		public MyThread(Configuration config, int threadNum, GroundPlanningProblem problem, List<SearchNode> cubes) {
			this.config = config;
			this.threadNum = threadNum;
			this.baseProblem = problem;
			this.localCubes = cubes;
			this.localPlan = null;
		}

		@Override
		public void run() {

			// Create planners
			List<SimplePlanner> planners = new ArrayList<SimplePlanner>();
			for (SearchNode cube : localCubes) {
				State tempState = cube.state;
				GroundPlanningProblem tempProblem = new GroundPlanningProblem(baseProblem);
				tempProblem.setInitialState(tempState);
				planners.add(new SimplePlanner(config, tempProblem));
			}

			int numExhausted = 0;
			// Round Robin over all Threads
			for (int i = 0; !Thread.interrupted(); i = (i + 1) % planners.size()) {
				if (numExhausted >= planners.size()) {
					// no Planner found a Plan
					break;
				} else if (planners.get(i).isExhausted()) {
					numExhausted++;
					continue;
				} else {
					numExhausted = 0;
					// System.out.printf("Thread %d running Planner %d out of %d planners.\n",
					// threadNum, i + 1, planners.size());
					localPlan = planners.get(i).calculateSteps(CUBE_ITERATIONS);
					if (localPlan != null) {
						Logger.log(Logger.INFO, "SimplePlanner \" (index " + threadNum + ") found a plan.");
						// System.out.printf("SimplePlanner \" (index " + threadNum + ") found a
						// plan.");

						// Concate Plan from cube to the plan of the SimplePlanner
						SearchNode node = localCubes.get(i);
						while (node != null && node.lastAction != null) {
							localPlan.appendAtFront(node.lastAction);
							node = node.parent;
						}

						onPlanFound(localPlan);
						break;
					}
				}
			}
		}
	}

	private class SimplePlanner {

		private State state;
		private Goal goal;
		private ActionIndex aindex;
		private SearchStrategy strategy;
		private SearchQueue frontier;
		private Plan plan;

		private int iterations;
		private boolean isExhausted;

		public SimplePlanner(Configuration config, GroundPlanningProblem problem) {

			state = new State(problem.getInitialState());
			goal = problem.getGoal();
			aindex = new ActionIndex(problem);

			strategy = new SearchStrategy(SearchStrategy.Mode.depthFirst);
			frontier = new SearchQueue(strategy);
			frontier.add(new SearchNode(null, state));
			plan = null;

			iterations = 0;
			isExhausted = false;
		}

		/**
		 * Tries to search for a plan with a greedy approach. Only uses the given amount
		 * of steps. Returns null if no plan is found or no plan exists. isExhausted()
		 * should be checked.
		 */
		public Plan calculateSteps(int steps) {

			int i = 0;
			while (!goal.isSatisfied(state) && i < steps && !Thread.currentThread().isInterrupted()
					&& !frontier.isEmpty()) {

				i++;
				iterations++;
				SearchNode node = frontier.get();

				// Is the goal reached?
				if (goal.isSatisfied(node.state)) {

					// Extract plan
					plan = new Plan();
					while (node != null && node.lastAction != null) {
						plan.appendAtFront(node.lastAction);
						node = node.parent;
					}
					// System.out.printf("Found a Plan after a total of %d iterations\n",
					// iterations);
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
			// no plan exists
			if (frontier.isEmpty()) {
				// System.out.printf("Some planner ran for %d iterations and found no plan.\n",
				// iterations);
				isExhausted = true;
			}
			// System.out.printf("Calculated %d steps\n", i);
			return null;
		}

		public boolean isExhausted() {
			return isExhausted;
		}
	}
}

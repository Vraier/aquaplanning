package edu.kit.aquaplanning.planning;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.planning.cube.ExponentialScheduler;
import edu.kit.aquaplanning.planning.cube.RoundRobinScheduler;
import edu.kit.aquaplanning.planning.cube.Scheduler;
import edu.kit.aquaplanning.planning.cube.Scheduler.ExitStatus;
import edu.kit.aquaplanning.planning.datastructures.ActionIndex;
import edu.kit.aquaplanning.planning.datastructures.SearchNode;
import edu.kit.aquaplanning.planning.datastructures.SearchQueue;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy;
import edu.kit.aquaplanning.util.Logger;

//TODO: outsource the search for cubes
//TODO: clear all those messy log and system.out entries
//TODO: check if synchronization works correctly
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
		List<Cube> cubes;

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
			List<Cube> localCubes = cubes.subList(partitionSize * i, Math.min(partitionSize * (i + 1), cubes.size()));

			Thread thread = new Thread(new MyThread(config, threadNum, localCubes));
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

	private List<Cube> findCubes(GroundPlanningProblem problem, int numCubes) {

		State initState = problem.getInitialState();
		Goal goal = problem.getGoal();
		ActionIndex aindex = new ActionIndex(problem);
		List<Cube> cubes;

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
		cubes = new ArrayList<Cube>();
		while (!frontier.isEmpty()) {
			cubes.add(new Cube(problem, frontier.get()));
		}
		return cubes;
	}

	private class MyThread implements Runnable {

		private Configuration config;
		private int threadNum;
		private List<Cube> localCubes;
		private Plan localPlan;

		public MyThread(Configuration config, int threadNum, List<Cube> cubes) {
			this.config = config;
			this.threadNum = threadNum;
			this.localCubes = cubes;
			this.localPlan = null;
		}

		@Override
		public void run() {

			// Create planners
			List<CubePlanner> planners = new ArrayList<CubePlanner>();
			for (Cube cube : localCubes) {
				planners.add(new ForwardSearchCubePlanner(config, cube));
			}

			// Initialize Scheduler
			Scheduler scheduler = new RoundRobinScheduler(planners, CUBE_ITERATIONS);
			//Scheduler scheduler = new ExponentialScheduler(planners, 1000, 2);

			// Search for a plan
			ExitStatus status = ExitStatus.foundNoPlan;
			while (!Thread.interrupted() && status != ExitStatus.exhausted && status != ExitStatus.foundPlan) {
				status = scheduler.scheduleNext();
			}
			if (status == ExitStatus.foundPlan) {
				int totalIterations = 0;
				localPlan = scheduler.getPlan();

				for (CubePlanner p : planners) {
					totalIterations += p.getTotalIterations();
				}

				System.out.printf("Thread %d found a Plan. The total sum of the iterations of his planners is %d.\n",
						threadNum, totalIterations);
				onPlanFound(localPlan);
			}
		}
	}
}

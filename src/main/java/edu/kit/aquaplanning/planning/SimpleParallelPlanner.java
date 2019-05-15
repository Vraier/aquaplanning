package edu.kit.aquaplanning.planning;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.Configuration.HeuristicType;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.planning.cube.CubeFinder;
import edu.kit.aquaplanning.planning.cube.CubePlanner;
import edu.kit.aquaplanning.planning.cube.ExponentialScheduler;
import edu.kit.aquaplanning.planning.cube.ForwardSearchCubeFinder;
import edu.kit.aquaplanning.planning.cube.ForwardSearchCubePlanner;
import edu.kit.aquaplanning.planning.cube.Scheduler.ExitStatus;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy;
import edu.kit.aquaplanning.util.Logger;

//TODO: clear all those messy log and system.out entries
//TODO: check if synchronization works correctly
public class SimpleParallelPlanner extends Planner {

	private static final int NUM_CUBES = 50;
	private static final int CUBE_ITERATIONS = 5000;
	private int numThreads;
	private List<Thread> threads;
	private Plan plan = null;

	public SimpleParallelPlanner(Configuration config) {
		super(config);
		numThreads = config.numThreads;
	}

	@Override
	public Plan findPlan(GroundPlanningProblem problem) {
		startSearch();

		threads = new ArrayList<>();
		Random random = new Random(this.config.seed); // seed generator
		List<Cube> cubes;

		Logger.log(Logger.INFO, "Starting to search for " + NUM_CUBES + " cubes.");
		// System.out.println("Starting to search for " + NUM_CUBES + " cubes");
		Configuration newConfig = config.copy();
		newConfig.searchStrategy = SearchStrategy.Mode.aStar;
		newConfig.heuristic = HeuristicType.ffWilliams;
		
		// Search for the Cubes
		CubeFinder cFinder = new ForwardSearchCubeFinder(newConfig);
		
		cubes = cFinder.findCubes(problem, NUM_CUBES);
		// check if we already found a Plan.
		plan = cFinder.getPlan();
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
		System.out.println("Found " + cubes.size() + " cubes");

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
				Configuration newConfig = config.copy();
				newConfig.searchStrategy = SearchStrategy.Mode.aStar;
				newConfig.heuristic = HeuristicType.manhattanGoalDistance;
				planners.add(new ForwardSearchCubePlanner(newConfig, cube));
			}

			// Initialize Scheduler
			//Scheduler scheduler = new RoundRobinScheduler(planners, CUBE_ITERATIONS);
			ExponentialScheduler scheduler = new ExponentialScheduler(planners);
			scheduler.setTime(200, 2);

			// Search for a plan
			ExitStatus status = ExitStatus.foundNoPlan;
			while (!Thread.interrupted() && status != ExitStatus.exhausted && status != ExitStatus.foundPlan) {
				status = scheduler.scheduleNext();
				assert(status != ExitStatus.error);
			}
			if (status == ExitStatus.foundPlan) {
				localPlan = scheduler.getPlan();

				int totalIterations = 0;
				long totalTime = 0;
				for (CubePlanner p : planners) {
					totalIterations += p.getTotalIterations();
					totalTime += p.getTotalTime();
				}

				System.out.printf("Thread %d found a Plan. The total sum of the iterations is %d and time is %d millisecs.\n",
						threadNum, totalIterations, totalTime);
				onPlanFound(localPlan);
			}
		}
	}
}

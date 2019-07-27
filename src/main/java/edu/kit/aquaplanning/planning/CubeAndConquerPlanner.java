package edu.kit.aquaplanning.planning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.planning.cube.CubeSolver;
import edu.kit.aquaplanning.planning.cube.ForwardSearchCubeSolver;
import edu.kit.aquaplanning.planning.cube.finder.CubeFinder;
import edu.kit.aquaplanning.planning.cube.scheduler.Scheduler;
import edu.kit.aquaplanning.planning.cube.scheduler.Scheduler.ExitStatus;
import edu.kit.aquaplanning.util.CSVWriter;
import edu.kit.aquaplanning.util.Logger;

public class CubeAndConquerPlanner extends Planner {

	private int numThreads;
	private List<Thread> threads;
	private Plan plan = null;

	/**
	 * Creates a new parallel planner. This planner uses a cube and conquer
	 * approach. The planner can be modified by the given configuration.
	 * 
	 * @param config
	 *            holds options to modify this planner
	 */
	public CubeAndConquerPlanner(Configuration config) {
		super(config);
		numThreads = config.numThreads;
	}

	@Override
	public Plan findPlan(GroundPlanningProblem problem) {
		startSearch();

		CSVWriter.setFolder(config.csvOutputFolder);

		Random random;
		List<Cube> cubes;
		CubeFinder cFinder;

		List<String> entrie1 = Arrays.asList("problemFile", "maxSeconds", "numThreads", "numCubes", "cubeFinderMode",
				"cubeNodeType", "cubePercent", "cubeSparceInterval", "schedulerMode", "schedulerIterations",
				"schedulerTime", "schedulerExponentialGrowth", "schedulerHillClimb", "cubeFindHeuristic",
				"cubeFindeHeuristicWeight", "cubeFindSearchStrategy", "cubeSolveHeuristic", "cubeSolveHeuristicWeight",
				"cubeSolveSearchStrategy");
		List<String> entrie2 = Arrays.asList(config.problemFile, Integer.toString(config.maxTimeSeconds),
				Integer.toString(config.numThreads), Integer.toString(config.numCubes),
				config.cubeFinderMode.toString(), config.cubeNodeType.toString(), Double.toString(config.cubePercent),
				Integer.toString(config.cubeSparseInterval), config.schedulerMode.toString(),
				Integer.toString(config.schedulerIterations), Long.toString(config.schedulerTime),
				Double.toString(config.schedulerGrowth), Double.toString(config.schedulerHillClimb),
				config.cubeFindHeuristic.toString(), Integer.toString(config.cubeFindHeuristicWeight),
				config.cubeFindSearchStrategy.toString(), config.cubeSolveHeuristic.toString(),
				Integer.toString(config.cubeSolveHeuristicWeight), config.cubeSolveSearchStrategy.toString());
		CSVWriter.writeFile("configurationInfo.csv", Arrays.asList(entrie1, entrie2));

		// Search for cubes
		Logger.log(Logger.INFO, "Starting to search for cubes.");
		cFinder = CubeFinder.getCubeFinder(config);
		cubes = cFinder.findCubes(problem);
		cFinder.logInformation();

		// check if we already found a Plan
		plan = cFinder.getPlan();
		if (plan != null) {
			Logger.log(Logger.INFO, "Already found a plan while searching for cubes.");
			return plan;
		} else if (cubes.size() == 0) {
			Logger.log(Logger.INFO, "Unable to find any cubes. Problem has no solution.");
			return null;
		}

		// Shuffle Cubes
		Logger.log(Logger.INFO, "Shuffleing the cubes.");
		random = new Random(config.seed);
		java.util.Collections.shuffle(cubes, random);

		// Split cubes evenly
		threads = new ArrayList<Thread>();
		for (int i = 0; i < numThreads; i++) {

			// Default configuration with random seed
			Configuration config = this.config.copy();
			config.seed = random.nextInt();
			int threadNum = i + 1;

			// Round up integer division to partition cubes evenly
			int partitionSize = ((cubes.size() + numThreads - 1) / numThreads);
			List<Cube> localCubes = cubes.subList(Math.min(partitionSize * i, cubes.size()),
					Math.min(partitionSize * (i + 1), cubes.size()));

			Logger.log(Logger.INFO, "Initializing Thread " + threadNum + ".");
			Thread thread = new Thread(new MyThread(config, threadNum, localCubes));
			threads.add(thread);

			// Start the planner (non-blocking call)
			Logger.log(Logger.INFO, "Starting Thread " + threadNum + " with " + localCubes.size() + " cubes.");
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
			// when withinTimeLimit() is checked the next time.
			thread.interrupt();
		}
	}

	/**
	 * Encapsulates the logic for the Threads of our Cube and Conquer approach. The
	 * task of a thread is given a list of cubes to try to solve them. Some form of
	 * distributing the calculation time between the cubes should be ensured.
	 */
	private class MyThread implements Runnable {

		private Configuration config;

		private int threadNum;
		private List<CubeSolver> planners;
		private Scheduler scheduler;
		private Plan localPlan = null;

		public MyThread(Configuration config, int threadNum, List<Cube> cubes) {
			this.config = config;
			this.threadNum = threadNum;

			// Create planners
			planners = new ArrayList<CubeSolver>();
			for (Cube cube : cubes) {
				planners.add(new ForwardSearchCubeSolver(config, cube));
			}

			// Initialize Scheduler
			scheduler = Scheduler.getScheduler(config, planners, threadNum);
		}

		@Override
		public void run() {

			// Search for a plan
			Logger.log(Logger.INFO, "Thread " + threadNum + " starting to work for the first time.");
			ExitStatus status = ExitStatus.foundNoPlan;
			while (withinTimeLimit() && status != ExitStatus.exhausted && status != ExitStatus.foundPlan) {
				status = scheduler.scheduleNext();
				assert (status != ExitStatus.error);
			}

			// Our Thread found a plan
			if (status == ExitStatus.foundPlan) {
				localPlan = scheduler.getPlan();
				Logger.log(Logger.INFO, "Thread " + threadNum + " found a Plan.");
				onPlanFound(localPlan);
			}
			// We got interrupted or couldn't find a plan
			else {
				Logger.log(Logger.INFO,
						"Thread " + threadNum + " found no Plan. The interruptFlag is: "
								+ Thread.currentThread().isInterrupted() + " and the exit status of the scheduler is "
								+ status + ".");
			}
			scheduler.logInformation();
		}

		/**
		 * Checks if we are in computational bounds. This means that our thread is not
		 * interrupted and we didn't exceed our time limit. The time limit is given by
		 * the configuration. There is no possibility to limit the cube solving by
		 * iterations.
		 */
		protected boolean withinTimeLimit() {

			if (Thread.currentThread().isInterrupted())
				return false;

			if (config.maxTimeSeconds > 0) {
				long totalTime = System.currentTimeMillis() - config.startTimeMillis;
				if (totalTime > config.maxTimeSeconds * 1000) {
					return false;
				}
			}

			return true;
		}
	}
}

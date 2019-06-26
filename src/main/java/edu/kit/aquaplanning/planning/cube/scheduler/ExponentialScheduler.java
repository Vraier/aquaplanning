package edu.kit.aquaplanning.planning.cube.scheduler;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.planning.cube.CubeSolver;
import edu.kit.aquaplanning.util.Logger;

public class ExponentialScheduler extends Scheduler {

	private List<CubeSolver> runningPlanners;

	// Computational Bounds Variables
	private int initialIterations = 0;
	private long initialTime = 0;
	private double exponentialGrowth = 0;

	private int queueIndex = 0;
	private int queueCycles = -1;
	private int addedPlanners = 0;
	
	public ExponentialScheduler(Configuration config, List<CubeSolver> planners) {
		super(config, planners);
		this.initialIterations = config.schedulerIterations;
		this.initialTime = config.schedulerTime;
		this.exponentialGrowth = config.schedulerGrowth;
		this.runningPlanners = new ArrayList<CubeSolver>();
	}

	@Override
	public ExitStatus scheduleNext() {

		totalScheduled++;
		
		if (initialIterations == 0 && initialTime == 0) {
			return ExitStatus.error;
		}

		// All Planners for the Queue ran one time. Add a new Planner to the
		// runningQueue if possible
		if (queueIndex >= runningPlanners.size()) {

			// Check if we already added all cubes from the readyQueue
			if (addedPlanners < planners.size()) {
				runningPlanners.add(planners.get(addedPlanners));
				addedPlanners++;
			}
			// Start from the beginning of the runningQueue again
			queueIndex = 0;
			queueCycles++;
		}

		// No planners left to run
		if (runningPlanners.size() == 0) {
			return ExitStatus.exhausted;

		} else if (runningPlanners.get(queueIndex).isExhausted()) {
			runningPlanners.remove(queueIndex);
			return scheduleNext();

		} else {

			CubeSolver currentPlanner = runningPlanners.get(queueIndex);

			if (initialIterations > 0) {
				int currentIterations = initialIterations * (int) Math.pow(exponentialGrowth, queueCycles - queueIndex);
				currentPlanner.setIterationLimit(currentIterations);
			}
			if (initialTime > 0) {
				long currentTime = initialTime * (long) Math.pow(exponentialGrowth, queueCycles - queueIndex);
				currentPlanner.setTimeLimit(currentTime);
			}

			plan = currentPlanner.calculateSteps();

			queueIndex++;
			if (plan != null) {
				return ExitStatus.foundPlan;
			}
			return ExitStatus.foundNoPlan;
		}
	}

	@Override
	public void logInformation() {
		long totalIterations = 0;
		long totalTime = 0;

		for (CubeSolver p : planners) {
			totalIterations += p.getTotalIterations();
			totalTime += p.getTotalTime();
		}
		Logger.log(Logger.INFO,
				"Exponential Scheduler scheduled a total of " + totalScheduled
						+ " cubes. The total sum of the iterations is " + totalIterations + " and time is " + totalTime
						+ " millisecs.");

	}
}

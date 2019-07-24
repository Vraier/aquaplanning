package edu.kit.aquaplanning.planning.cube.scheduler;

import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.planning.cube.CubeSolver;
import edu.kit.aquaplanning.util.Logger;

public class RoundRobinScheduler extends Scheduler {

	private int numExhausted = 0;
	private int nextRunning = 0;

	private int iterations = 0;
	private long time = 0;

	/**
	 * Creates a new round robin scheduler. The scheduler will only start working
	 * after its computational bounds variable have been set by the according
	 * methods
	 * 
	 * @param planners
	 *            The planners to schedule
	 */
	public RoundRobinScheduler(Configuration config, List<CubeSolver> planners, int id) {
		super(config, planners,id);
		this.iterations = config.schedulerIterations;
		this.time = config.schedulerTime;
	}

	@Override
	public ExitStatus scheduleNext() {

		if (iterations == 0 && time == 0) {
			return ExitStatus.error;

		} else if (plan != null) {
			return ExitStatus.foundPlan;

		} else if (numExhausted >= planners.size()) {
			// no Planner found a Plan
			return ExitStatus.exhausted;

		} else if (planners.get(nextRunning).isExhausted()) {
			numExhausted++;
			nextRunning = (nextRunning + 1) % planners.size();
			return this.scheduleNext();

		} else {
			// found a feasible Planner
			numExhausted = 0;

			CubeSolver currentPlanner = planners.get(nextRunning);
			if (iterations > 0) {
				currentPlanner.setIterationLimit(iterations);
			}
			if (time > 0) {
				currentPlanner.setTimeLimit(time);
			}

			plan = currentPlanner.calculateSteps();
			totalScheduled++;

			nextRunning = (nextRunning + 1) % planners.size();
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
				"Round Robin Scheduler scheduled a total of " + totalScheduled
						+ " cubes. The total sum of the iterations is " + totalIterations + " and time is " + totalTime
						+ " millisecs.");
	}
}

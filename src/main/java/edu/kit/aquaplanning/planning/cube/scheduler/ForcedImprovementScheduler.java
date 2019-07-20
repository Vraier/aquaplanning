package edu.kit.aquaplanning.planning.cube.scheduler;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.planning.cube.CubeSolver;
import edu.kit.aquaplanning.util.Logger;

public class ForcedImprovementScheduler extends Scheduler {

	List<CubeSolver> solvers;
	List<Integer> scheduleCount;

	private int iterations = 0;
	private long time = 0;
	private int numExhausted = 0;
	private int nextRunning = 0;

	public ForcedImprovementScheduler(Configuration config, List<CubeSolver> planners) {
		super(config, planners);

		this.solvers = planners;
		this.scheduleCount = new ArrayList<Integer>();
		for (int i = 0; i < planners.size(); i++) {
			scheduleCount.add(0);
		}
		this.iterations = config.schedulerIterations;
		this.time = config.schedulerTime;
	}

	@Override
	public ExitStatus scheduleNext() {

		if (nextRunning == 0) {
			solvers.sort((s1, s2) -> Integer.compare(s1.getBestDistance(), s2.getBestDistance()));
		}

		if (iterations == 0 && time == 0) {
			return ExitStatus.error;

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

			int initialHeuristic = currentPlanner.getBestDistance();
			plan = currentPlanner.calculateSteps();
			scheduleCount.set(nextRunning, scheduleCount.get(nextRunning) + 1);
			totalScheduled++;

			if (plan != null) {
				return ExitStatus.foundPlan;

			} else if (initialHeuristic - currentPlanner.getBestDistance() <= 0) {
				// we reached a local plateau and try the next solver
				nextRunning = (nextRunning + 1) % planners.size();
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
				"Forced Improvment Scheduler scheduled a total of " + totalScheduled
						+ " cubes. The total sum of the iterations is " + totalIterations + " and time is " + totalTime
						+ " millisecs.");
		Logger.log(Logger.INFO,
				"The following list represents the count of schedulings for each solver: " + scheduleCount.toString());
	}
}

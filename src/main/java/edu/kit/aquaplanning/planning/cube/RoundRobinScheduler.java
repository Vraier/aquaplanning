package edu.kit.aquaplanning.planning.cube;

import java.util.List;

import edu.kit.aquaplanning.model.ground.Plan;

public class RoundRobinScheduler implements Scheduler {

	private List<CubePlanner> planners;
	private Plan plan;
	private int numExhausted = 0;
	private int nextRunning = 0;

	private int iterations = 0;
	private long time = 0;

	public RoundRobinScheduler(List<CubePlanner> planners) {
		this.planners = planners;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public void setTime(long time) {
		this.time = time;
	}

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

			CubePlanner currentPlanner = planners.get(nextRunning);
			if (iterations > 0) {
				currentPlanner.setIterationLimit(iterations);
			}
			if (time > 0) {
				currentPlanner.setTimeLimit(time);
			}

			plan = currentPlanner.calculateSteps();

			nextRunning = (nextRunning + 1) % planners.size();
			if (plan != null) {
				return ExitStatus.foundPlan;
			}
			return ExitStatus.foundNoPlan;
		}
	}

	public Plan getPlan() {
		return plan;
	}
}

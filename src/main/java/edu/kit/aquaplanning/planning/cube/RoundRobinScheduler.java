package edu.kit.aquaplanning.planning.cube;

import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.ground.Plan;

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
	 * @param planners The planners to schedule
	 */
	public RoundRobinScheduler(Configuration config, List<CubeSolver> planners) {
		super(config, planners);
		this.iterations = config.schedulerIterations;
		this.time = config.schedulerTime;
	}

	/**
	 * Sets the amount of iterations each planner gets in one slice. If time is set
	 * too the planner will be bounded by both parameters.
	 * 
	 * @param iterations the number of iterations for each slice
	 */
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	/**
	 * Sets the amount of time each planner gets in one slice. If iterations are set
	 * too the planner will be bounded by both parameters.
	 * 
	 * @param time time for each slice
	 */
	public void setTime(long time) {
		this.time = time;
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

			nextRunning = (nextRunning + 1) % planners.size();
			if (plan != null) {
				return ExitStatus.foundPlan;
			}
			return ExitStatus.foundNoPlan;
		}
	}

	@Override
	public Plan getPlan() {
		return plan;
	}
}

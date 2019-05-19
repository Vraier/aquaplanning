package edu.kit.aquaplanning.planning.cube;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.ground.Plan;

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

	public void setIterations(int initialIterations, double exponentialGrowth) {
		this.initialIterations = initialIterations;
		this.exponentialGrowth = exponentialGrowth;
	}

	public void setTime(long initialTime, double exponentialGrowth) {
		this.initialTime = initialTime;
		this.exponentialGrowth = exponentialGrowth;
	}

	@Override
	public ExitStatus scheduleNext() {

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
			// TODO: maybe watch out for integer overflow. But the runtime probably kills us
			// anyways if we get near an overflow.

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
	public Plan getPlan() {
		return plan;
	}
}

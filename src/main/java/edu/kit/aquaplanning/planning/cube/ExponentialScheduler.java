package edu.kit.aquaplanning.planning.cube;

import java.util.ArrayList;
import java.util.List;

import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.planning.CubePlanner;

public class ExponentialScheduler implements Scheduler {

	private List<CubePlanner> planners;
	private List<CubePlanner> runningPlanners;
	private int initialIterations;
	private int queueIndex;
	private int queueCycles;
	private Plan plan;
	private double exponentialGrowth;

	public ExponentialScheduler(List<CubePlanner> planners, int initialIterations, double exponentialGrowth) {
		this.planners = planners;
		this.runningPlanners = new ArrayList<CubePlanner>();
		this.queueIndex = 0;
		this.queueCycles = -1;
		this.initialIterations = initialIterations;
		this.exponentialGrowth = exponentialGrowth;
	}

	@Override
	public ExitStatus scheduleNext() {

		// All Planners for the Queue ran one time. Add a new Planner to the
		// runningQueue if possible
		if (queueIndex >= runningPlanners.size()) {
			if (runningPlanners.size() < planners.size()) {
				runningPlanners.add(planners.get(runningPlanners.size()));
			}
			// Start for the beginning of the runningQueue again
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
			int currentIterations = initialIterations * (int) Math.pow(exponentialGrowth, queueCycles - queueIndex);
			CubePlanner currentPlanner = runningPlanners.get(queueIndex);
			currentPlanner.setIterationLimit(currentIterations);
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

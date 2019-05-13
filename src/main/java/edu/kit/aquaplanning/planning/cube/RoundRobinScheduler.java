package edu.kit.aquaplanning.planning.cube;

import java.util.List;

import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.planning.CubePlanner;

public class RoundRobinScheduler implements Scheduler {

	private List<CubePlanner> planners;
	private int numIterations;
	private int numExhausted;
	private int nextRunning;
	private Plan plan;

	public RoundRobinScheduler(List<CubePlanner> planners, int numIterations) {
		this.planners = planners;
		this.numIterations = numIterations;
		this.numExhausted = 0;
		this.nextRunning = 0;
	}

	public ExitStatus scheduleNext() {

		if (plan != null) {
			return ExitStatus.foundPlan;

		} else if (numExhausted >= planners.size()) {
			// no Planner found a Plan
			return ExitStatus.exhausted;

		} else if (planners.get(nextRunning).isExhausted()) {
			numExhausted++;
			nextRunning = (nextRunning + 1) % planners.size();
			return this.scheduleNext();

		} else {
			numExhausted = 0;
			
			CubePlanner currentPlanner = planners.get(nextRunning);
			currentPlanner.setIterationLimit(numIterations);
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

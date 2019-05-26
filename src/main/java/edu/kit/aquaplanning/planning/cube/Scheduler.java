package edu.kit.aquaplanning.planning.cube;

import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.ground.Plan;

public abstract class Scheduler {

	protected Configuration config;
	protected List<CubeSolver> planners;
	protected Plan plan = null;

	public Scheduler(Configuration config, List<CubeSolver> planners) {
		this.config = config;
		this.planners = planners;
	}

	/**
	 * Enum to retrieve information to the user of the schduleNext() method.
	 */
	public enum ExitStatus {

		exhausted,

		foundPlan,

		foundNoPlan,

		error;
	}

	public abstract ExitStatus scheduleNext();

	public abstract Plan getPlan();

	public static Scheduler getScheduler(Configuration config, List<CubeSolver> planners) {
		switch (config.schedulerMode) {
		case exponential:
			return new ExponentialScheduler(config, planners);
		case roundRobin:
			return new RoundRobinScheduler(config, planners);
		default:
			break;
		}
		return null;
	}
}
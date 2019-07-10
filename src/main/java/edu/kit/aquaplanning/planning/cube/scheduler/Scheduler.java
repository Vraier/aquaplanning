package edu.kit.aquaplanning.planning.cube.scheduler;

import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.planning.cube.CubeSolver;

public abstract class Scheduler {

	protected Configuration config;
	protected List<CubeSolver> planners;
	protected Plan plan = null;
	protected int totalScheduled = 0;

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

	public Plan getPlan() {
		return plan;
	}

	public static Scheduler getScheduler(Configuration config, List<CubeSolver> planners) {
		switch (config.schedulerMode) {
		case exponential:
			return new ExponentialScheduler(config, planners);
		case roundRobin:
			return new RoundRobinScheduler(config, planners);
		case bandit:
			return new BanditScheduler(config, planners);
		case hillClimbing:
			return new HillClimbingScheduler(config, planners);
		default:
			throw new UnsupportedOperationException("Scheduler " + config.schedulerMode + " is not available");
		}
	}

	public abstract void logInformation();
}

package edu.kit.aquaplanning.planning.cube.scheduler;

import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.planning.cube.CubeSolver;

public abstract class Scheduler {

	protected Configuration config;
	protected int id;
	protected List<CubeSolver> planners;
	protected Plan plan = null;
	protected int totalScheduled = 0;

	public Scheduler(Configuration config, List<CubeSolver> planners, int id) {
		this.config = config;
		this.planners = planners;
		this.id = id;
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

	public static Scheduler getScheduler(Configuration config, List<CubeSolver> planners, int id) {
		switch (config.schedulerMode) {
		case exponential:
			return new ExponentialScheduler(config, planners, id);
		case roundRobin:
			return new RoundRobinScheduler(config, planners, id);
		case bandit:
			return new BanditScheduler(config, planners, id);
		case greedyBandit:
			return new GreedyBanditScheduler(config, planners, id);
		case forcedImprovement:
			return new ForcedImprovementScheduler(config, planners, id);
		case hillClimbing:
			return new HillClimbingScheduler(config, planners, id);
		default:
			throw new UnsupportedOperationException("Scheduler " + config.schedulerMode + " is not available");
		}
	}

	public abstract void logInformation();
}

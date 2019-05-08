package edu.kit.aquaplanning.planning.cube;

import edu.kit.aquaplanning.model.ground.Plan;

//TODO: rethink this interface
//TODO: add an option to use the config file to create a scheduler
public interface Scheduler {

	public enum ExitStatus {

		exhausted,

		foundPlan,

		foundNoPlan;
	}

	public ExitStatus scheduleNext();

	public Plan getPlan();

}

package edu.kit.aquaplanning.planning.cube.scheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.planning.cube.CubeSolver;
import edu.kit.aquaplanning.util.Logger;

public class HillClimbingScheduler extends Scheduler {

	private LinkedList<MySolver> solvers = new LinkedList<>();
	private ArrayList<MySolver> originalSolvers = new ArrayList<>();
	private double percent;

	private int iterations = 0;
	private long time = 0;

	public HillClimbingScheduler(Configuration config, List<CubeSolver> planners) {
		super(config, planners);
		this.iterations = config.schedulerIterations;
		this.time = config.schedulerTime;
		this.percent = config.schedulerHillClimb;

		for (CubeSolver c : planners) {
			MySolver s = new MySolver(c);
			solvers.add(s);
		}
		originalSolvers = new ArrayList<MySolver>(solvers);
	}

	@Override
	public ExitStatus scheduleNext() {

		if (iterations == 0 && time == 0) {
			return ExitStatus.error;
		} else if (plan != null) {
			return ExitStatus.foundPlan;
		} else if (solvers.size() == 0) {
			// no Planner found a Plan
			return ExitStatus.exhausted;
		}

		// current candidate
		MySolver candidate = solvers.peek();

		// we reached a local maximum and put the solver at the end of our queue
		if (candidate.getAverageDifference() < getTotalAverage() * percent) {
			solvers.poll();
			solvers.add(candidate);
			return scheduleNext();
		}
		// found a feasible planner
		else {
			if (iterations > 0) {
				candidate.solver.setIterationLimit(iterations);
			}
			if (time > 0) {
				candidate.solver.setTimeLimit(time);
			}
			// calculate steps and add the new difference to the average
			int currentDistance = candidate.solver.getBestDistance();
			plan = candidate.solver.calculateSteps();
			if (plan != null) {
				return ExitStatus.foundPlan;
			} else if (candidate.solver.isExhausted()) {
				// delete from list
				solvers.poll();
			} else {
				candidate.addDifference(currentDistance - candidate.solver.getBestDistance());
			}
			return ExitStatus.foundNoPlan;
		}
	}

	@Override
	public void logInformation() {
		originalSolvers.sort((s1, s2) -> Integer.compare(s2.differences.size(), s1.differences.size()));
		StringBuilder out = new StringBuilder();
		for (MySolver s : originalSolvers.subList(0, Math.min(10, originalSolvers.size()))) {
			out.append(s.differences.toString() + "\n");
		}
		Logger.log(Logger.INFO, "The 10 most scheduled solvers have a difference list of\n" + out.toString());
	}

	/**
	 * Returns the average of all differences of all solvers and 0 if no solver has
	 * a difference.
	 */
	private double getTotalAverage() {
		int sum = 0;
		int count = 0;
		for (MySolver s : solvers) {
			sum += s.sum;
			count += s.differences.size();
		}
		if (count == 0) {
			return 0;
		} else {
			return (double) sum / (double) count;
		}
	}

	private class MySolver {

		ArrayList<Integer> differences = new ArrayList<>();
		int sum = 0;
		CubeSolver solver;

		MySolver(CubeSolver solver) {
			this.solver = solver;
		}

		void addDifference(int difference) {
			differences.add(difference);
			sum += difference;
		}

		double getAverageDifference() {
			if (differences.size() == 0) {
				return Integer.MAX_VALUE;
			} else {
				return (double) sum / (double) differences.size();
			}
		}
	}
}

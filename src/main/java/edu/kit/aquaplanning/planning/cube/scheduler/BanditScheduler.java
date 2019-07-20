package edu.kit.aquaplanning.planning.cube.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.planning.cube.CubeSolver;
import edu.kit.aquaplanning.util.Logger;

public class BanditScheduler extends Scheduler {

	// computation time for solvers
	private int iterations;
	private long time;
	private long maxTime;

	private List<Bandit> bandits;

	public BanditScheduler(Configuration config, List<CubeSolver> planners) {
		super(config, planners);
		this.iterations = config.schedulerIterations;
		this.time = config.schedulerTime;
		this.maxTime = config.maxTimeSeconds;

		if (this.iterations > 0) {
			throw new IllegalArgumentException("Bandit Scheduling after iterations not implemented yet.");
		}

		// initialize bandits
		bandits = new ArrayList<Bandit>();
		for (CubeSolver c : planners) {
			assert (!c.isExhausted());
			Bandit newBandit = new Bandit(c);
			newBandit.addReward(newBandit.getSolver().getBestDistance());
			bandits.add(newBandit);
		}
	}

	@Override
	public ExitStatus scheduleNext() {
		if (totalScheduled < bandits.size()) {
			return initialSchedule();
		} else {
			return informedSchedule();
		}
	}

	private ExitStatus initialSchedule() {

		// System.out.println(
		// "We have " + bandits.size() + " bandits and do the " + (totalScheduled + 1) +
		// " initial schedule.");
		Bandit bandit = bandits.get(totalScheduled);
		CubeSolver solver = bandit.getSolver();
		assert (!solver.isExhausted());

		// Set up computational bounds
		if (iterations > 0) {
			solver.setIterationLimit(iterations);
		}
		if (time > 0) {
			solver.setTimeLimit(time);
		}

		// initialize rewards for bandits
		plan = solver.calculateSteps();
		bandit.addReward(solver.getBestDistance());
		totalScheduled++;

		if (plan != null) {
			return ExitStatus.foundPlan;
		}
		return ExitStatus.foundNoPlan;
	}

	private ExitStatus informedSchedule() {
		CubeSolver bestSolver = null;
		Bandit bestBandit = null;
		double bestReward = -1;

		// Find the best Bandit in the list.
		for (Bandit b : bandits) {
			CubeSolver currPlanner = b.getSolver();
			if (currPlanner.isExhausted()) {
				continue;
			}

			double currReward = b.calcConfidenceBound(totalScheduled);
			if (currReward > bestReward) {
				bestReward = currReward;
				bestBandit = b;
			}
		}

		if (bestBandit == null) {
			return ExitStatus.exhausted;
		}
		bestSolver = bestBandit.getSolver();

		// Set up computational bounds
		if (iterations > 0) {
			bestSolver.setIterationLimit(iterations);
		}
		if (time > 0) {
			bestSolver.setTimeLimit(time);
		}

		// start solving and update rewards
		plan = bestSolver.calculateSteps();
		totalScheduled++;
		bestBandit.addReward(bestSolver.getBestDistance());
		
		if (plan != null) {
			return ExitStatus.foundPlan;
		}
		return ExitStatus.foundNoPlan;
	}

	@Override
	public void logInformation() {
		long totalIterations = 0;
		long totalTime = 0;

		for (CubeSolver p : planners) {
			totalIterations += p.getTotalIterations();
			totalTime += p.getTotalTime();
		}
		Logger.log(Logger.INFO,
				"Bandit Scheduler scheduled a total of " + totalScheduled
						+ " cubes. The total sum of the iterations is " + totalIterations + " and time is " + totalTime
						+ " millisecs.");

		bandits.sort((b1, b2) -> b2.rewards.size() - b1.rewards.size());
		Logger.log(Logger.INFO,
				"The the 10 most played Bandits got played " + bandits.subList(0, Math.min(10, bandits.size())).stream()
						.map(b -> b.rewards.size() - 1).collect(Collectors.toList()) + " times.");
		System.out
				.println("The the 10 most played Bandits got played " + bandits.subList(0, Math.min(10, bandits.size()))
						.stream().map(b -> b.rewards.size() - 1).collect(Collectors.toList()) + " times.");

		if (totalScheduled >= bandits.size()) {
			Logger.log(Logger.INFO, "They have a reward value of " + bandits.subList(0, Math.min(10, bandits.size()))
					.stream().map(b -> b.calcConfidenceBound(totalScheduled)).collect(Collectors.toList()));
			Logger.log(Logger.INFO, "They have a heuristic List of" + bandits.subList(0, Math.min(10, bandits.size()))
					.stream().map(b -> b.rewards.toString()).collect(Collectors.toList()));
			System.out.println("They have a reward value of " + bandits.subList(0, Math.min(10, bandits.size()))
					.stream().map(b -> b.calcConfidenceBound(totalScheduled)).collect(Collectors.toList()));
			System.out.println("They have a heuristic List of" + bandits.subList(0, Math.min(10, bandits.size()))
					.stream().map(b -> b.rewards.toString()).collect(Collectors.toList()));
		}
	}

	private class Bandit {

		private CubeSolver planner;

		private List<Double> rewards = new ArrayList<Double>();

		Bandit(CubeSolver planner) {
			this.planner = planner;
		}

		public CubeSolver getSolver() {
			return planner;
		}

		public double calcConfidenceBound(int totalPlayed) {
			// TODO play with some factors?
			return 2.0 * calcEstimatedReward() + 0.5 * calcBias(totalPlayed);
		}

		public void addReward(double currentDistance) {
			assert (currentDistance >= 0) : "The added distance must be >= 0 but is " + currentDistance;
			rewards.add(currentDistance);
		}

		/**
		 * Calculate the estimated finishing time by calculating the regression line for
		 * all prior points and scale the result between 0 and 1.
		 * 
		 * @return a normalized estimated for the finishing time t (0 t > upperBound and
		 *         1 if t = 0)
		 */
		private double calcEstimatedReward() {
			assert (rewards.size() >= 2) : "Reward list only hods " + rewards.size()
					+ " item but should hold at least 2.";
			assert (maxTime > 0) : "MaxTime must be greater than 0 to allow for upperBound of calculation time.";
			double upperBound = maxTime * 1000;
			double xHat = (rewards.size() - 1) * time / 2.0;
			double yHat = 0;
			for (Double d : rewards) {
				yHat += d;
			}
			yHat /= rewards.size();

			double divident = 0;
			double divisor = 0;
			for (int i = 0; i < rewards.size(); i++) {
				divident += (i * time - xHat) * (rewards.get(i) - yHat);
				divisor += (i * time - xHat) * (i * time - xHat);
			}
			// y = a + x * b
			double b = divident / divisor;
			double a = yHat - b * xHat;

			double estimatedFinishingTime;
			if (b >= 0) {
				// our estimated finishing time would be infinite (or negative)
				estimatedFinishingTime = upperBound;
			} else {
				// solve for 0 = a + x * b.
				estimatedFinishingTime = -a / b > upperBound ? upperBound : -a / b;
			}
			// normalize result
			assert (estimatedFinishingTime >= 0);
			return (upperBound - estimatedFinishingTime) / upperBound;
		}

		private double calcBias(int totalPlayed) {
			return Math.sqrt((2 * Math.log((double) totalPlayed)) / (rewards.size() - 1));
		}
	}
}

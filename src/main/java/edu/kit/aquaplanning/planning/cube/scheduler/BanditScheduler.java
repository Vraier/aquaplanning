package edu.kit.aquaplanning.planning.cube.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.planning.cube.CubeSolver;
import edu.kit.aquaplanning.util.Logger;

public class BanditScheduler extends Scheduler {

	private int iterations;
	private long time;

	private List<Bandit> bandits;
	private int timesPlayed = 0;
	private double startingDistance = 0;

	public BanditScheduler(Configuration config, List<CubeSolver> planners) {
		super(config, planners);
		this.iterations = config.schedulerIterations;
		this.time = config.schedulerTime;
		bandits = new ArrayList<Bandit>();

		// initialize bandit and get the worst distance of all planners
		for (CubeSolver c : planners) {
			bandits.add(new Bandit(c));
			if (c.getBestDistance() > startingDistance) {
				startingDistance = c.getBestDistance();
			}
		}

		// Add first distance to bandits
		for (Bandit b : bandits) {
			assert (!b.planner.isExhausted());
			b.addReward(startingDistance, b.planner.getBestDistance());
		}
		
		timesPlayed = bandits.size();
	}

	@Override
	public ExitStatus scheduleNext() {

		CubeSolver bestSolver = null;
		Bandit bestBandit = null;
		double bestReward = 0;

		// Find the best Bandit in the list and remove exhausted ones.
		for (Bandit b : bandits) {
			CubeSolver currPlanner = b.getSolver();
			if (currPlanner.isExhausted()) {
				bandits.remove(b);
				continue;
			}

			double currReward = b.calcConfidenceBound(timesPlayed);
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
		bestBandit.addReward(startingDistance, bestSolver.getBestDistance());
		timesPlayed++;

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
				"Bandit Scheduler scheduled a total of " + timesPlayed + " cubes. The total sum of the iterations is "
						+ totalIterations + " and time is " + totalTime + " millisecs.");
		bandits.sort((b1, b2) -> b2.rewards.size() - b1.rewards.size());
		Logger.log(Logger.INFO,
				"The the 10 most played Bandits got played " + bandits.subList(0, Math.min(10, bandits.size())).stream()
						.map(b -> b.rewards.size()).collect(Collectors.toList()) + " times.");
		System.out
				.println("The the 10 most played Bandits got played " + bandits.subList(0, Math.min(10, bandits.size()))
						.stream().map(b -> b.rewards.size()).collect(Collectors.toList()) + " times.");
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
			return calcAverageReward() + calcBias(totalPlayed);
		}

		/**
		 * Adds a new reward to this bandit. A reward should be between 1 and 0. It take
		 * the current estimate of the bandit and the original worst distance and adds a
		 * normalize value to this bandit.
		 * 
		 * @param startingDistance
		 *            the original worst estimated distance to the goal
		 * @param currentDistance
		 *            the current estimated distance to the goal
		 */
		public void addReward(double startingDistance, double currentDistance) {
			if (startingDistance == 0) {
				rewards.add(0.0);
			} else {
				double reward = (startingDistance - currentDistance) / startingDistance;
				rewards.add(reward);
			}
		}

		private double calcAverageReward() {
			double sum = 0;
			assert (rewards.size() > 0);
			for (Double d : rewards) {
				sum += d;
			}
			return sum / rewards.size();
		}

		private double calcBias(int totalPlayed) {
			return Math.sqrt((2 * Math.log((double) totalPlayed)) / rewards.size());
		}
	}
}

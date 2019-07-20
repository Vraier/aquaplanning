package edu.kit.aquaplanning.planning.cube.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.planning.cube.CubeSolver;
import edu.kit.aquaplanning.util.Logger;

public class GreedyBanditScheduler extends Scheduler {
	// computation time for solvers
	private int iterations;
	private long time;
	private long maxTime;

	// original bandits are stored for first computations. afterwards PQ is used
	private List<Bandit> originalBandits;
	private PriorityQueue<Bandit> bandits;

	public GreedyBanditScheduler(Configuration config, List<CubeSolver> planners) {
		super(config, planners);
		this.iterations = config.schedulerIterations;
		this.time = config.schedulerTime;
		this.maxTime = config.maxTimeSeconds;

		if (this.iterations > 0) {
			throw new IllegalArgumentException("Greedy Bandit Scheduling after iterations not implemented yet.");
		}

		// initialize bandits
		originalBandits = new ArrayList<Bandit>();
		bandits = new PriorityQueue<Bandit>(
				(b1, b2) -> Double.compare(b1.getEstimatedComputationTime(), b2.getEstimatedComputationTime()));

		for (CubeSolver c : planners) {
			assert (!c.isExhausted());
			Bandit newBandit = new Bandit(c);
			newBandit.addReward(newBandit.getSolver().getBestDistance());
			originalBandits.add(newBandit);
		}
	}

	@Override
	public ExitStatus scheduleNext() {
		if (totalScheduled < originalBandits.size()) {
			return initialSchedule();
		} else {
			return informedSchedule();
		}
	}

	private ExitStatus initialSchedule() {

		Bandit bandit = originalBandits.get(totalScheduled);
		CubeSolver solver = bandit.getSolver();
		assert (!solver.isExhausted());

		// Set up computational bounds
		if (iterations > 0) {
			solver.setIterationLimit(iterations);
		}
		if (time > 0) {
			solver.setTimeLimit(time);
		}

		// initialize rewards for bandits and insert into PQ
		plan = solver.calculateSteps();
		bandit.addReward(solver.getBestDistance());
		totalScheduled++;
		if (!solver.isExhausted()) {
			bandits.add(bandit);
		}

		if (plan != null) {
			return ExitStatus.foundPlan;
		}
		return ExitStatus.foundNoPlan;
	}

	private ExitStatus informedSchedule() {

		CubeSolver bestSolver;
		Bandit bestBandit = bandits.poll();

		// queue is empty
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

		// start solving and update PQ accordingly
		plan = bestSolver.calculateSteps();
		bestBandit.addReward(bestSolver.getBestDistance());
		totalScheduled++;
		if (!bestSolver.isExhausted()) {
			bandits.add(bestBandit);
		}

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
				"Greedy Bandit Scheduler scheduled a total of " + totalScheduled
						+ " cubes. The total sum of the iterations is " + totalIterations + " and time is " + totalTime
						+ " millisecs.");

		originalBandits.sort((b1, b2) -> b2.heuristicValues.size() - b1.heuristicValues.size());
		Logger.log(Logger.INFO,
				"The the 10 most played Bandits got played "
						+ originalBandits.subList(0, Math.min(10, originalBandits.size())).stream()
								.map(b -> b.heuristicValues.size() - 1).collect(Collectors.toList())
						+ " times.");
		System.out
				.println(
						"The the 10 most played Bandits got played "
								+ originalBandits.subList(0, Math.min(10, originalBandits.size())).stream()
										.map(b -> b.heuristicValues.size() - 1).collect(Collectors.toList())
								+ " times.");

		if (totalScheduled >= originalBandits.size()) {
			Logger.log(Logger.INFO,
					"They have a estimated compuation time of "
							+ originalBandits.subList(0, Math.min(10, originalBandits.size())).stream()
									.map(b -> b.getEstimatedComputationTime()).collect(Collectors.toList()));
			Logger.log(Logger.INFO,
					"They have a heuristic List of" + originalBandits.subList(0, Math.min(10, originalBandits.size()))
							.stream().map(b -> b.heuristicValues.toString()).collect(Collectors.toList()));
			Logger.log(Logger.INFO,
					"They have a remaining time List of"
							+ originalBandits.subList(0, Math.min(10, originalBandits.size())).stream()
									.map(b -> b.remainingTime.toString()).collect(Collectors.toList()));
			
			System.out.println("They have a estimated compuation time of "
					+ originalBandits.subList(0, Math.min(10, originalBandits.size())).stream()
							.map(b -> b.getEstimatedComputationTime()).collect(Collectors.toList()));
			System.out.println(
					"They have a heuristic List of" + originalBandits.subList(0, Math.min(10, originalBandits.size()))
							.stream().map(b -> b.heuristicValues.toString()).collect(Collectors.toList()));
			System.out.println("They have a remaining time List of"
					+ originalBandits.subList(0, Math.min(10, originalBandits.size())).stream()
							.map(b -> b.remainingTime.toString()).collect(Collectors.toList()));
		}
	}

	private class Bandit {

		private CubeSolver planner;

		private List<Double> heuristicValues = new ArrayList<Double>();
		private List<Double> remainingTime = new ArrayList<Double>();

		Bandit(CubeSolver planner) {
			this.planner = planner;
		}

		public CubeSolver getSolver() {
			return planner;
		}

		public void addReward(double currentDistance) {
			assert (currentDistance >= 0) : "The added distance must be >= 0 but is " + currentDistance;
			heuristicValues.add(currentDistance);
			if (heuristicValues.size() >= 2) {
				remainingTime.add(calcEstimatedComputationTime());
			}
		}

		public double getEstimatedComputationTime() {
			return remainingTime.get(remainingTime.size() - 1);
		}

		/**
		 * Calculate the estimated remaining computation time for solving the problem by
		 * calculating the regression line for all prior points.
		 */
		private double calcEstimatedComputationTime() {
			assert (heuristicValues.size() >= 2) : "Reward list only hods " + heuristicValues.size()
					+ " item but should hold at least 2.";
			assert (maxTime > 0) : "MaxTime must be greater than 0 to allow for upperBound of calculation time.";

			// TODO magic number for upper bound
			double upperBound = maxTime * 1000;
			double xHat = (heuristicValues.size() - 1) * time / 2.0;
			double yHat = 0;
			for (Double d : heuristicValues) {
				yHat += d;
			}
			yHat /= heuristicValues.size();

			double divident = 0;
			double divisor = 0;
			for (int i = 0; i < heuristicValues.size(); i++) {
				divident += (i * time - xHat) * (heuristicValues.get(i) - yHat);
				divisor += (i * time - xHat) * (i * time - xHat);
			}
			// y = a + x * b
			double b = divident / divisor;
			double a = yHat - b * xHat;

			double estimatedFinishingTime;
			double remainingFinishingTime;

			if (b >= 0) {
				// our estimated finishing time would be infinite (or negative)
				estimatedFinishingTime = upperBound;
			} else {
				// solve for 0 = a + x * b.
				estimatedFinishingTime = -a / b > upperBound ? upperBound : -a / b;
			}

			// Calculate how much time is left to solve problem
			remainingFinishingTime = estimatedFinishingTime - time * (heuristicValues.size() - 1);
			// assert (remainingFinishingTime >= 0);

			return remainingFinishingTime;
		}
	}
}

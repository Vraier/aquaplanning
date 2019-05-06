package edu.kit.aquaplanning.planning;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.Atom;
import edu.kit.aquaplanning.model.ground.AtomSet;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.planning.datastructures.ActionIndex;
import edu.kit.aquaplanning.planning.datastructures.FullActionIndex;
import edu.kit.aquaplanning.planning.datastructures.SearchNode;
import edu.kit.aquaplanning.planning.datastructures.SearchQueue;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy;
import edu.kit.aquaplanning.util.Logger;

public class SimpleParallelPlanner extends Planner {

	private static final int NUM_CUBES = 80;
	private static final int CUBE_ITERATIONS = 5000000;
	private int numThreads;
	private List<Thread> threads;
	private Plan plan;

	private int iteration;

	public SimpleParallelPlanner(Configuration config) {
		super(config);
		numThreads = config.numThreads;
	}

	@Override
	public Plan findPlan(GroundPlanningProblem problem) {
		startSearch();

		iteration = 0;
		threads = new ArrayList<>();
		plan = null;
		List<SearchNode> cubes;
		Random random = new Random(this.config.seed); // seed generator

		Logger.log(Logger.INFO, "Starting to search for " + NUM_CUBES + " cubes.");
		System.out.println("Starting to search for " + NUM_CUBES + " cubes");
		cubes = findCubes(problem, NUM_CUBES);

		if (plan != null) {
			Logger.log(Logger.INFO, "Already found a plan while searching for cubes.");
			System.out.println("Already found a plan while searching for cubes.");
			return plan;
		} else if (cubes == null) {
			Logger.log(Logger.INFO, "Error occured while searching for cubes. Unable to find any cubes.");
			System.out.println("Error occured while searching for cubes. Unable to find any cubes.");
			return null;
		}
		Logger.log(Logger.INFO, "Found " + cubes.size() + " cubes.");
		System.out.println("Found " + cubes.size() + " cubes");

		java.util.Collections.shuffle(cubes, random);

		for (int i = 0; i < numThreads; i++) {

			// Default configuration with random seed
			Configuration config = this.config.copy();
			config.seed = random.nextInt();
			int threadNum = i;

			// partition the cubes into equal parts
			// Round up integer division
			int partitionSize = ((cubes.size() + numThreads - 1) / numThreads);
			List<SearchNode> localCubes = cubes.subList(partitionSize * i,
					Math.min(partitionSize * (i + 1), cubes.size()));

			Thread thread = new Thread(new MyThread(config, threadNum, problem, localCubes));
			threads.add(thread);

			// Start the planner (non-blocking call)
			thread.start();
		}

		// Wait for all threads to finish
		// (if some plan has been found, all threads are interrupted)
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Plan is not null iff any planner was successful
		return plan;
	}

	/**
	 * Callback for when some planner finds a plan.
	 */
	private synchronized void onPlanFound(Plan plan) {
		if (this.plan != null) {
			// Another planner already found a plan
			return;
		}
		this.plan = plan;
		// Interrupt all planners
		for (Thread thread : threads) {
			// This interruption is acknowledged inside each planner thread
			// when withinComputationalBounds() is checked the next time.
			thread.interrupt();
		}
	}

	private List<SearchNode> findCubes(GroundPlanningProblem problem, int numCubes) {

		State initState = problem.getInitialState();
		Goal goal = problem.getGoal();
		ActionIndex aindex = new ActionIndex(problem);
		List<SearchNode> cubes;

		// Initialize breadthFirst search
		// TODO: implement own breadthFirstSerach for more controll over datastructures
		SearchStrategy strategy = new SearchStrategy(SearchStrategy.Mode.breadthFirst);
		SearchQueue frontier = new SearchQueue(strategy);
		frontier.add(new SearchNode(null, initState));

		while (withinComputationalBounds(iteration) && !frontier.isEmpty() && frontier.size() < numCubes) {

			// Visit node (by the heuristic provided to the priority queue)
			SearchNode node = frontier.get();

			// Is the goal reached?
			if (goal.isSatisfied(node.state)) {

				// Extract plan
				Plan plan = new Plan();
				while (node != null && node.lastAction != null) {
					plan.appendAtFront(node.lastAction);
					node = node.parent;
				}
				return null;
			}

			// Expand node: iterate over operators
			for (Action action : aindex.getApplicableActions(node.state)) {
				// Create new node by applying the operator
				State newState = action.apply(node.state);

				// Add new node to frontier
				SearchNode newNode = new SearchNode(node, newState);
				newNode.lastAction = action;
				frontier.add(newNode);
			}

			iteration++;
		}

		if (frontier.isEmpty()) {
			return new ArrayList<SearchNode>();
		} else if (!withinComputationalBounds(iteration)) {
			return null;
		} else {

			// retrieve all nodes from the queue
			cubes = new ArrayList<SearchNode>();
			while (!frontier.isEmpty()) {
				cubes.add(frontier.get());
			}
			return cubes;
		}
	}

	private class MyThread implements Runnable {

		// TODO: implement means to interrupt Threads
		Configuration config;
		int threadNum;
		GroundPlanningProblem problem;
		List<SearchNode> localCubes;

		public MyThread(Configuration config, int threadNum, GroundPlanningProblem problem, List<SearchNode> cubes) {
			this.config = config;
			this.threadNum = threadNum;
			this.problem = problem;
			this.localCubes = cubes;
		}

		@Override
		public void run() {

			// Create planners
			List<SimplePlanner> planners = new ArrayList<SimplePlanner>();
			for (SearchNode cube : localCubes) {
				State tempState = cube.state;
				GroundPlanningProblem tempProblem = new GroundPlanningProblem(problem);
				tempProblem.setInitialState(tempState);
				planners.add(new SimplePlanner(config, tempProblem));
			}

			Plan localPlan = null;
			int numExhausted = 0;
			//Round Robin over all Threads
			for (int i = 0; !Thread.interrupted(); i = (i + 1) % planners.size()) {
				if (numExhausted >= planners.size()) {
					// no Planner found a Plan
					break;
				} else if (planners.get(i).isExhausted()) {
					numExhausted++;
					continue;
				} else {
					numExhausted = 0;
					System.out.printf("Thread %d running Planner %d out of %d planners.\n", threadNum, i, planners.size());
					localPlan = planners.get(i).calculateSteps(CUBE_ITERATIONS);
					if (localPlan != null) {
						Logger.log(Logger.INFO, "SimplePlanner \" (index " + threadNum + ") found a plan.");
						onPlanFound(localPlan);
					}
				}
			}
		}
	}

	private class SimplePlanner {

		private ArrayDeque<State> stateHistory;
		private ArrayDeque<Action> plan;
		// visitedStates = new MoveToFrontHashTable(64*1024*1024);
		private HashSet<AtomSet> visitedStates;
		private FullActionIndex aindex;
		private State state;
		private Goal goal;
		private Collection<Action> applicableActions;
		private int iterations = 0;
		private Random rnd;
		private boolean isExhausted;

		public SimplePlanner(Configuration config, GroundPlanningProblem problem) {

			stateHistory = new ArrayDeque<>();
			plan = new ArrayDeque<>();
			visitedStates = new HashSet<>();
			aindex = new FullActionIndex(problem);
			state = new State(problem.getInitialState());
			goal = problem.getGoal();
			applicableActions = aindex.getApplicableActions(state);
			iterations = 0;
			isExhausted = false;
			rnd = new Random(config.seed);
		}

		/**
		 * Tries to search for a plan with a greedy approach. Only uses the given amount of steps.
		 * Returns null if no plan is found or no plan exists. isExhausted() should be checked.
		 */
		public Plan calculateSteps(int steps) {
			
			int i = 0;
			while (!goal.isSatisfied(state) || i < steps || !Thread.currentThread().isInterrupted()) {
				i++;
				iterations++;
				visitedStates.add(state.getAtomSet());
				Action best = null;
				int bestValue = -1;

				for (Action a : applicableActions) {
					State newState = a.apply(state);
					if (visitedStates.contains(newState.getAtomSet())) {
						continue;
					} else {
						int value = calculateManhattan(newState, goal);
						if (value > bestValue) {
							bestValue = value;
							best = a;
						}
					}
				}

				if (best == null) {
					if (plan.size() == 0) {
						// Plan does not exist
						System.out.printf("Some planner ran for %d iterations and found no plan.\n", iterations);
						isExhausted = true;
						return null;
					}
					// backtracking
					plan.removeLast();
					State newState = stateHistory.pollLast();
					updateApplicableActionsChanges(applicableActions, state, newState, aindex);
					state = newState;
				} else {
					// select the best action
					plan.addLast(best);
					stateHistory.addLast(state);
					State newState = best.apply(state);
					updateApplicableActionsChanges(applicableActions, state, newState, aindex);
					state = newState;
				}
			}

			System.out.println("HELLO");
			if (goal.isSatisfied(state)) {
				// make the plan
				Plan finalplan = new Plan();
				for (Action a : plan) {
					finalplan.appendAtBack(a);
				}
				Logger.log(Logger.INFO, String.format(
						"successfull greedy search, visited %d states, did %d iterations, found plan of length %d",
						visitedStates.size(), iterations, plan.size()));
				return finalplan;
			} else {
				System.out.printf("Calculated %d steps", i);
				return null;
			}
		}

		private void updateApplicableActionsChanges(Collection<Action> actions, State oldState, State newState,
				FullActionIndex aindex) {
			// first remove actions that are no more applicable
			Iterator<Action> iter = actions.iterator();
			while (iter.hasNext()) {
				Action a = iter.next();
				if (!a.isApplicable(newState)) {
					iter.remove();
				}
			}
			// add new applicable actions for changed state variables
			if (aindex.getNoPrecondActions() != null) {
				for (Action a : aindex.getNoPrecondActions()) {
					if (a.isApplicable(newState)) {
						actions.add(a);
					}
				}
			}
			// Check and debug
			AtomSet changes = oldState.getAtomSet().xor(newState.getAtomSet());
			int changeId = changes.getFirstTrueAtom();
			while (changeId != -1) {
				int precondIndex = newState.getAtomSet().get(changeId) ? changeId + 1 : -changeId - 1;
				List<Action> cands = aindex.getActionsWithPrecondition(precondIndex);
				if (cands != null) {
					for (Action a : cands) {
						if (a.isApplicable(newState)) {
							actions.add(a);
						}
					}
				}
				changeId = changes.getNextTrueAtom(changeId + 1);
			}
		}

		private int calculateManhattan(State state, Goal goal) {
			int satisfiedGoals = 0;
			for (Atom g : goal.getAtoms()) {
				if (state.holds(g)) {
					satisfiedGoals++;
				}
			}
			return 10 * (satisfiedGoals) + rnd.nextInt(10);
		}

		public boolean isExhausted() {
			return isExhausted;
		}
	}
}

package edu.kit.aquaplanning.planning.cube.finder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.Atom;
import edu.kit.aquaplanning.model.ground.AtomSet;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.planning.cube.datastructure.BackwardSearchNode;
import edu.kit.aquaplanning.util.Logger;

//TODO: update intersect/node methods of the AtomSet class
public class BackwardSearchCubeFinder extends CubeFinder {

	Queue<BackwardSearchNode> queue;
	Set<BackwardSearchNode> visitedGoals;

	public BackwardSearchCubeFinder(Configuration config) {
		super(config);
	}

	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem, int numCubes) {

		State startingState = problem.getInitialState();
		BackwardSearchNode startNode = new BackwardSearchNode(problem.getGoal());
		List<Action> actions = problem.getActions();

		queue = new ArrayDeque<BackwardSearchNode>();
		visitedGoals = new HashSet<BackwardSearchNode>();
		queue.add(startNode);
		visitedGoals.add(startNode);

		while (!queue.isEmpty() && queue.size() <= numCubes) {

			BackwardSearchNode node = queue.poll();

			// Check if our current goal is already satisfied by the starting node
			if (satisfiesGoal(startingState, node)) {
				plan = node.getPartialPlan();
				// no cubes to return because we already found a node
				return null;
			}

			for (Action a : actions) {

				// We found a new possible goal. Add it to the queue
				if (canApplyTo(a, node)) {
					BackwardSearchNode newNode = getPredecessor(a, node);
					add(newNode);
				}
			}
		}

		if (queue.size() < numCubes) {
			Logger.log(Logger.INFO, "Something went not so good. Backwardsearch didnt find enough goals!");
		}
		List<Cube> cubes = new ArrayList<Cube>();
		for (BackwardSearchNode n : queue) {
			Goal newGoal;
			Cube newCube;
			List<Atom> atomList = new ArrayList<Atom>();

			// Generate new Goals form the AtomSets
			for (int i = 0; i < problem.getAtomNames().size(); i++) {
				if (n.trueAtoms.get(i)) {
					atomList.add(new Atom(i, problem.getAtomNames().get(i), true));
				}
				if (n.falseAtoms.get(i)) {
					atomList.add(new Atom(i, problem.getAtomNames().get(i), false));
				}
			}
			newGoal = new Goal(atomList);
			newCube = new Cube(problem, newGoal, n.getPartialPlan());
			cubes.add(newCube);
		}
		return cubes;

	}

	@Override
	public Plan getPlan() {
		return plan;
	}

	/**
	 * Checks if applying the given action to a state can result in the given goal.
	 * 
	 * 
	 * Returns true iff the effects of the given action are a subset of the given
	 * goal. This means that all true atoms in the effects of the action are set in
	 * the goal and all negative atoms are not set.
	 */
	private static boolean canApplyTo(Action action, BackwardSearchNode node) {

		AtomSet tSet = action.getEffectsPos();
		AtomSet fSet = action.getEffectsNeg();

		// return n.trueAtoms.all(pSet) && n.falseAtoms.all(nSet); This does not work
		return (node.trueAtoms.intersects(tSet) || node.falseAtoms.intersects(fSet)) && !node.trueAtoms.intersects(fSet)
				&& !node.falseAtoms.intersects(tSet);
	}

	/**
	 * Constructs the predecessor of a backward search node given the action that
	 * leads to this node. Since a backward search node represents a goal (a set of
	 * states) only one predecessor is enough to represent all possible states that
	 * lead to this goal.
	 * 
	 * @param a
	 *            the action that will fulfill the goal provided by the node
	 * @param n
	 *            the node that should be fulfilled by the action
	 * @return a goal from which the action will fulfill the node
	 */
	private static BackwardSearchNode getPredecessor(Action a, BackwardSearchNode n) {

		assert (canApplyTo(a, n));

		AtomSet tAtoms = (AtomSet) n.trueAtoms.clone();
		tAtoms.applyTrueAtomsAsFalse(a.getEffectsPos());
		tAtoms.applyTrueAtoms(a.getPreconditionsPos());

		AtomSet fAtoms = (AtomSet) n.falseAtoms.clone();
		fAtoms.applyTrueAtomsAsFalse(a.getEffectsNeg());
		fAtoms.applyTrueAtoms(a.getPreconditionsNeg());

		assert (!fAtoms.intersects(tAtoms));

		return new BackwardSearchNode(tAtoms, fAtoms, n, a);
	}

	private void add(BackwardSearchNode node) {

		if (!visitedGoals.contains(node)) {
			queue.add(node);
			visitedGoals.add(node);
		}
	}

	/**
	 * state trueAtoms = t, node trueAtoms = t', node falseAtoms = f'
	 * 
	 * @return (t' subset t and f' not intersects t)<=>(t' subset t and f' subset f)
	 */
	private static boolean satisfiesGoal(State state, BackwardSearchNode node) {
		return state.getAtomSet().all(node.trueAtoms) && state.getAtomSet().none(node.falseAtoms);
	}
}

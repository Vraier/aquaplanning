package edu.kit.aquaplanning.planning.cube;

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
import edu.kit.aquaplanning.util.Logger;

// TODO: check if node satisfies the startingState. If so we are done and
// already found a plan. This is pretty resource intensive though.
// TODO: when do we stop the backwardSearch. What happens in the special cases?
public class BackwardSearchCubeFinder extends CubeFinder {

	Queue<BackwardSearchNode> queue;
	Set<BackwardSearchNode> visitedGoals;

	public BackwardSearchCubeFinder(Configuration config) {
		super(config);
	}

	@Override
	public List<Cube> findCubes(GroundPlanningProblem problem, int numCubes) {

		queue = new ArrayDeque<BackwardSearchNode>();
		visitedGoals = new HashSet<BackwardSearchNode>();

		// State startingState = problem.getInitialState();
		BackwardSearchNode startNode = new BackwardSearchNode(problem.getGoal());
		queue.add(startNode);
		visitedGoals.add(startNode);
		List<Action> actions = problem.getActions();

		while (!queue.isEmpty() && queue.size() <= numCubes) {

			BackwardSearchNode node = queue.poll();
			// System.out.println("I poll a node " + node.trueAtoms + " " + node.falseAtoms
			// + ".");

			for (Action a : actions) {

				// We found a new possible goal. Add it to the queue
				if (canApplyTo(a, node)) {
					// System.out.println("I found an action: " + a + ".");
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
		// TODO at the moment this feature is not implemented.
		return null;
	}

	/**
	 * Checks if applying the given action to a state can result in the given goal.
	 * 
	 * 
	 * Returns true iff the effects of the given action are a subset of the given
	 * goal. This means that all true atoms in the effects of the action are set in
	 * the goal and all negative atoms are not set.
	 */
	private boolean canApplyTo(Action action, BackwardSearchNode node) {

		AtomSet tSet = action.getEffectsPos();
		AtomSet fSet = action.getEffectsNeg();

		// return n.trueAtoms.all(pSet) && n.falseAtoms.all(nSet); This does not work
		return (node.trueAtoms.intersects(tSet) || node.falseAtoms.intersects(fSet)) && !node.trueAtoms.intersects(fSet)
				&& !node.falseAtoms.intersects(tSet);
	}

	private BackwardSearchNode getPredecessor(Action a, BackwardSearchNode n) {

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
			// System.out.println("I add a node " + node.trueAtoms + " " + node.falseAtoms +
			// ".");
			queue.add(node);
			visitedGoals.add(node);
		}
	}
}
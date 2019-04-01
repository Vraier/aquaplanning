package edu.kit.aquaplanning.planning.htn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.kit.aquaplanning.grounding.HtnGrounder;
import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.Atom;
import edu.kit.aquaplanning.model.ground.AtomSet;
import edu.kit.aquaplanning.model.ground.Goal;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.ground.Precondition;
import edu.kit.aquaplanning.model.ground.Precondition.PreconditionType;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.model.ground.htn.HierarchyLayer;
import edu.kit.aquaplanning.model.ground.htn.Reduction;
import edu.kit.aquaplanning.sat.IpasirSatSolver;
import edu.kit.aquaplanning.sat.SatSolver;
import edu.kit.aquaplanning.util.BinaryEncoding;
import edu.kit.aquaplanning.util.Logger;

public class TreeRexPlanner {
	
	public enum SolverMode {
		MODE_SAT4J, MODE_IPASIR;
	}
	
	private HtnGrounder grounder;
	private int depth;

	private SolverMode solverMode;
	SatSolver sat4jSolver;
	IpasirSatSolver ipasirSolver;
	
	public TreeRexPlanner(HtnGrounder grounder) {
		this.grounder = grounder;
	}
	
	public Plan findPlan() {
		
		this.solverMode = SolverMode.MODE_SAT4J;
		
		if (solverMode == SolverMode.MODE_IPASIR) {
			ipasirSolver = new IpasirSatSolver();
		} else {
			sat4jSolver = new SatSolver();
		}
		
		depth = 0;
		assumptions = new ArrayList<>();
		
		Logger.log(Logger.INFO, "Generating depth " + depth);

		HierarchyLayer newLayer = grounder.getHierarchyLayers().get(depth);
		
		Logger.log(Logger.INFO, "Adding clauses ...");
		
		// Initial clauses (init task network)
		addInitTaskNetwork(newLayer, 0, grounder.getInitReduction());
		addInitState(newLayer);
		addGoal(newLayer);
		
		// Universal clauses
		for (int pos = 0; pos+1 < newLayer.getSize(); pos++) {
			addReductionClauses(newLayer, pos);
			Map<Integer, List<Action>> support = addActionClauses(newLayer, pos);
			addFrameAxioms(newLayer, pos, support);
		}
		
		// Goal assumption
		assumeFinishedPlan(newLayer);
		
		Logger.log(Logger.INFO, "Solving ...");
		Boolean result = solve();
		
		while (result == false) {
			
			depth++;
			Logger.log(Logger.INFO, "Generating depth " + depth);
			HierarchyLayer oldLayer = newLayer;
			grounder.computeNextLayer();
			newLayer = grounder.getHierarchyLayers().get(depth);
			
			Logger.log(Logger.INFO, "Adding clauses ...");
			
			// Transitional clauses oldLayer -> newLayer
			addPropagations(oldLayer, newLayer);
			addExpansions(oldLayer, newLayer);
			
			// Universal clauses for new layer
			for (int pos = 0; pos+1 < newLayer.getSize(); pos++) {
				addReductionClauses(newLayer, pos);
				Map<Integer, List<Action>> support = addActionClauses(newLayer, pos);
				addFrameAxioms(newLayer, pos, support);
			}
			
			// Goal assumption
			assumeFinishedPlan(newLayer);

			Logger.log(Logger.INFO, "Solving ...");
			result = solve();
		}
		
		if (result == true) {
			Logger.log(Logger.INFO, "Extracting plan ...");
			Plan plan = extractPlan();
			if (solverMode == SolverMode.MODE_IPASIR)
				ipasirSolver.release();
			return plan;
		} else {
			return null;
		}
	}
	
	private void addInitState(HierarchyLayer layer) {
		
		State initState = grounder.getGroundProblem().getInitialState();
		List<Boolean> atoms = initState.getAtoms();
		for (int i = 0; i < atoms.size(); i++) {
			addClause((atoms.get(i) ? 1 : -1) * layer.getFactVariable(0, i+1));
		}
	}
	
	private void addGoal(HierarchyLayer layer) {
		
		Goal goal = grounder.getGroundProblem().getGoal();
		for (Atom atom : goal.getAtoms()) {
			addClause((atom.getValue() ? 1 : -1) * layer.getFactVariable(layer.getSize()-1, atom.getId()+1));
		}
	}
	
	/**
	 * Add all before/after/between constraints and non-primitiveness of a reduction
	 */
	private void addInitTaskNetwork(HierarchyLayer layer, int pos, Reduction r) {
		
		for (int p = 0; p < r.getNumSubtasks(); p++) {
			
			String subtask = r.getSubtask(p);
			for (Action a : grounder.getActions(subtask)) {
				addToClause(layer.getActionVariable(pos+p, a));
			}
			for (Reduction red : grounder.getReductions(subtask)) {
				addToClause(layer.getReductionVariable(pos+p, red));
			}
			finishClause();
			
			Precondition constraint = r.getConstraint(p);
			addCondition(layer, pos+p, constraint, 0);
		}
		
		Precondition constraint = r.getConstraint(r.getNumSubtasks());
		addCondition(layer, pos+r.getNumSubtasks(), constraint, 0);
	}
	
	/**
	 * Add all before/after/between constraints and non-primitiveness of reductions
	 */
	private void addReductionClauses(HierarchyLayer layer, int pos) {
		
		for (Reduction r : layer.getReductions(pos)) {
			
			// If the reduction occurs, the position is non-primitive
			addClause(-layer.getReductionVariable(pos, r), -layer.getPrimitivenessVariable(pos));		
		}
	}
	
	/**
	 * Add primitiveness, preconditions and effects of each action and amo over actions
	 * Returns the supporting actions for each fact at that position
	 */
	private Map<Integer, List<Action>> addActionClauses(HierarchyLayer layer, int pos) {
		
		Map<Integer, List<Action>> supportingActions = new HashMap<>();
		
		for (Action a : layer.getActions(pos)) {			
			
			// If the action occurs, the position is primitive
			addClause(-layer.getActionVariable(pos, a), layer.getPrimitivenessVariable(pos));
			
			// Add preconditions and effects
			List<Integer> facts = new ArrayList<>();
			addCondition(layer, pos, a.getPreconditionsPos(), -layer.getActionVariable(pos, a), true, null);
			addCondition(layer, pos, a.getPreconditionsNeg(), -layer.getActionVariable(pos, a), false, null);
			addCondition(layer, pos+1, a.getEffectsPos(), -layer.getActionVariable(pos, a), true, facts);
			addCondition(layer, pos+1, a.getEffectsNeg(), -layer.getActionVariable(pos, a), false, facts);
			// Add action as support to all facts that may change due to it
			for (int fact : facts) {
				List<Action> support = supportingActions.getOrDefault(fact, new ArrayList<>());
				support.add(a);
				supportingActions.put(fact, support);
			}
		}
		
		List<Action> actions = new ArrayList<>();
		actions.addAll(layer.getActions(pos));
		BinaryEncoding enc = layer.getBinaryEncoding(pos);
		if (enc == null) {
			// Pairwise At-Most-One constraints
			for (int i = 0; i < actions.size(); i++) {
				for (int j = i+1; j < actions.size(); j++) {
					addClause(-layer.getActionVariable(pos, actions.get(i)), 
							-layer.getActionVariable(pos, actions.get(j)));
				}
			}
		} else {
			// Binary At-Most-One constraints
			enc.getForbiddenValues().forEach(clause -> addClause(clause));
			for (int i = 0; i < actions.size(); i++) {
				Action a = actions.get(i);
				int aLit = -layer.getActionVariable(pos, a);
				
				int[] bitLits = enc.posLiterals(i);
				for (int bitLit : bitLits) {
					addClause(aLit, bitLit);
				}
			}
		}
		
		return supportingActions;
	}
	
	/**
	 * Adds frame axioms from pos to pos+1.
	 */
	private void addFrameAxioms(HierarchyLayer layer, int pos, 
			Map<Integer, List<Action>> supportingActions) {
		
		for (int fact : layer.getFacts(pos+1)) {
			int posBefore = layer.getLatestPositionOfFactVariable(pos, fact);
			if (posBefore < 0) {
				// Fact never occurs!
				continue;
			}
			
			addToClause(layer.getFactVariable(posBefore, fact));
			addToClause(-layer.getFactVariable(pos+1, fact));
			for (int p = posBefore; p <= pos; p++) {				
				addToClause(-layer.getPrimitivenessVariable(p));
			}
			for (Action a : supportingActions.getOrDefault(fact, new ArrayList<>())) {
				addToClause(layer.getActionVariable(pos, a));
			}
			finishClause();
			
			addToClause(-layer.getFactVariable(posBefore, fact));
			addToClause(layer.getFactVariable(pos+1, fact));
			for (int p = posBefore; p <= pos; p++) {				
				addToClause(-layer.getPrimitivenessVariable(p));
			}
			for (Action a : supportingActions.getOrDefault(-fact, new ArrayList<>())) {
				addToClause(layer.getActionVariable(pos, a));
			}
			finishClause();
		}
	}
	
	/**
	 * Primitivity, actions, facts
	 */
	private void addPropagations(HierarchyLayer oldLayer, HierarchyLayer newLayer) {
		
		for (int pos = 0; pos < oldLayer.getSize(); pos++) {
			int successorPos = oldLayer.getSuccessorPosition(pos);
			
			addClause(-oldLayer.getPrimitivenessVariable(pos), newLayer.getPrimitivenessVariable(successorPos));
			
			for (Action a : oldLayer.getActions(pos)) {
				addClause(-oldLayer.getActionVariable(pos, a), newLayer.getActionVariable(successorPos, a));
			}
			
			for (int fact : oldLayer.getFacts(pos)) {
				addClause(-oldLayer.getFactVariable(pos, fact), newLayer.getFactVariable(successorPos, fact));
				addClause(oldLayer.getFactVariable(pos, fact), -newLayer.getFactVariable(successorPos, fact));
			}
		}
	}
	
	/**
	 * Reductions
	 */
	private void addExpansions(HierarchyLayer oldLayer, HierarchyLayer newLayer) {
		
		for (int pos = 0; pos < oldLayer.getSize(); pos++) {
		
			for (Reduction r : oldLayer.getReductions(pos)) {
				
				int successorPos = oldLayer.getSuccessorPosition(pos);
				int maxExpansionSize = oldLayer.getSuccessorPosition(pos+1) - successorPos;
				int p = 0;
				for (; p < r.getNumSubtasks(); p++) {
					
					addToClause(-oldLayer.getReductionVariable(pos, r));
					String subtask = r.getSubtask(p);
					for (Action action : grounder.getActions(subtask)) {
						addToClause(newLayer.getActionVariable(successorPos+p, action));
					}
					for (Reduction red : grounder.getReductions(subtask)) {
						addToClause(newLayer.getReductionVariable(successorPos+p, red));
					}
					finishClause();
				}
				// Fill empty positions with blank actions
				for (; p < maxExpansionSize; p++) {
					addClause(-oldLayer.getReductionVariable(pos, r), 
							newLayer.getActionVariable(successorPos+p, HierarchyLayer.BLANK_ACTION));
				}
				
				// Add reduction's constraints
				for (p = 0; p <= r.getNumSubtasks(); p++) {
					Precondition constraint = r.getConstraint(p);
					addCondition(newLayer, successorPos+p, constraint, 
							-oldLayer.getReductionVariable(pos, r));
				}
			}
		}
	}
	
	private void assumeFinishedPlan(HierarchyLayer newLayer) {
		
		for (int pos = 0; pos < newLayer.getSize(); pos++) {
			addAssumption(newLayer.getPrimitivenessVariable(pos));
		}
	}
	
	private void addCondition(HierarchyLayer layer, int pos, Precondition cond, int firstLit) {
		
		List<Precondition> conditions = new ArrayList<>();
		conditions.add(cond);
		for (int i = 0; i < conditions.size(); i++) {
			Precondition pre = conditions.get(i);
			if (pre.getType() == PreconditionType.atom) {
				Atom atom = pre.getAtom();
				int fact = atom.getId()+1;
				
				int posBefore = layer.getLatestPositionOfFactVariable(pos, fact);
				if (posBefore < pos) {
					System.out.println(atom + " : " + posBefore + " < " + pos);
					throw new RuntimeException();
				}
				
				if (firstLit != 0) {
					addToClause(firstLit);
				}
				addToClause((atom.getValue() ? 1 : -1) * layer.getFactVariable(posBefore, fact));
				finishClause();
				
			} else if (pre.getType() == PreconditionType.conjunction) {
				conditions.addAll(pre.getChildren());
			}
		}
	}
	
	private void addCondition(HierarchyLayer layer, int pos, AtomSet atoms, int firstLit, 
			boolean sign, List<Integer> facts) {
		
		for (int idx = atoms.getFirstTrueAtom(); idx >= 0; idx = atoms.getNextTrueAtom(idx+1)) {
			int fact = (sign ? 1 : -1) * (idx+1);
			
			int posBefore = layer.getLatestPositionOfFactVariable(pos, Math.abs(fact));
			if (posBefore < pos) {
				System.out.println(fact + " : " + posBefore + " < " + pos);
				throw new RuntimeException();
			}
			if (posBefore < 0) {
				continue;
			}
			
			if (firstLit != 0) {
				addToClause(firstLit);
			}
			addToClause(layer.getFactVariable(posBefore, fact));
			finishClause();
			
			if (facts != null)
				facts.add(fact);
		}
	}
	
	
	 
	List<Integer> currentClause;
	List<Integer> assumptions;
	
	private void addToClause(int lit) {
		if (currentClause == null)
			currentClause = new ArrayList<>();
		currentClause.add(lit);
	}
	
	private void finishClause() {
		int[] array = currentClause.stream().mapToInt(i->i).toArray();
		addClause(array);
		currentClause = null;
	}
	
	private void addClause(int... lits) {
		if (solverMode == SolverMode.MODE_IPASIR) {
			ipasirSolver.addClause(lits);
		} else {
			boolean success = sat4jSolver.addClause(lits);
			if (!success) {
				throw new RuntimeException("SAT solver reported contradiction while adding a clause.");
			}
		}
	}
	
	private void addAssumption(int lit) {
		if (solverMode == SolverMode.MODE_IPASIR) {			
			ipasirSolver.addAssumption(lit);
		} else {
			assumptions.add(lit);
		}
	}
	
	private Boolean solve() {
		if (solverMode == SolverMode.MODE_IPASIR) {
			return ipasirSolver.solve();			
		} else {
			int[] array = assumptions.stream().mapToInt(i->i).toArray();
			assumptions = new ArrayList<>();
			return sat4jSolver.isSatisfiable(array);
		}
	}
	
	private Plan extractPlan() {
		Plan plan = new Plan();
		
		HierarchyLayer finalLayer = grounder.getHierarchyLayers().get(depth);
		for (int pos = 0; pos < finalLayer.getSize(); pos++) {
			for (Action a : finalLayer.getActions(pos)) {
				if (a == HierarchyLayer.BLANK_ACTION)
					continue;
				int actionVar = finalLayer.getActionVariable(pos, a);
				if (solverMode == SolverMode.MODE_IPASIR && ipasirSolver.holds(actionVar)) {
					plan.appendAtBack(a);
				} else if (solverMode == SolverMode.MODE_SAT4J && sat4jSolver.getModel()[actionVar] > 0) {
					plan.appendAtBack(a);
				}
			}
		}
		
		return plan;
	}
}
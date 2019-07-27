package edu.kit.aquaplanning.aquaplanning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.grounding.Grounder;
import edu.kit.aquaplanning.grounding.PlanningGraphGrounder;
import edu.kit.aquaplanning.model.ground.Action;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.State;
import edu.kit.aquaplanning.parsing.ProblemParser;
import edu.kit.aquaplanning.planning.cube.datastructure.ForwardSearchNode;
import edu.kit.aquaplanning.planning.cube.datastructure.GenericSearchNode;
import junit.framework.TestCase;

public class TestCubeDatastructure extends TestCase {

	public void testForwardSearchNodeEquals() throws IOException {

		List<GroundPlanningProblem> problems = getProblems();

		for (GroundPlanningProblem problem : problems) {

			Set<GenericSearchNode> nodes = new HashSet<>();
			State initialState = problem.getInitialState();
			ForwardSearchNode inode = new ForwardSearchNode(problem);

			nodes.add(inode);
			assertTrue(nodes.size() == 1);

			List<GenericSearchNode> list1 = inode.getPredecessors();
			List<GenericSearchNode> list2 = inode.getPredecessors();

			List<GenericSearchNode> list3 = new ArrayList<>();
			for (Action a : problem.getActions()) {

				if (a.isApplicable(initialState)) {
					GroundPlanningProblem tempProb = new GroundPlanningProblem(problem);
					tempProb.setInitialState(a.apply(initialState));
					list3.add(new ForwardSearchNode(tempProb));
				}
			}

			assertTrue("List1 has size " + list1.size() + " and list3 hast size " + list3.size(),
					list1.size() == list3.size());

			nodes.addAll(list1);
			int size = nodes.size();
			nodes.addAll(list2);
			assertTrue("Expected size " + size + " but got " + nodes.size(), nodes.size() == size);
			nodes.addAll(list3);
			assertTrue("Expected size " + size + " but got " + nodes.size(), nodes.size() == size);
		}
	}

	private List<GroundPlanningProblem> getProblems() throws IOException {

		List<GroundPlanningProblem> result = new ArrayList<>();

		File benchdir = new File("testfiles");
		for (File domdir : benchdir.listFiles()) {

			String domain = domdir.getCanonicalPath() + "/domain.pddl";
			File testFile = new File(domain);
			if (!testFile.exists()) {
				continue;
			}

			for (File f : domdir.listFiles()) {
				if (f.getName().startsWith("p") && f.getName().endsWith(".pddl")) {
					String problem = f.getCanonicalPath();

					Grounder grounder = new PlanningGraphGrounder(new Configuration());
					result.add(grounder.ground(new ProblemParser().parse(domain, problem)));
				}
			}
		}
		return result;
	}
}

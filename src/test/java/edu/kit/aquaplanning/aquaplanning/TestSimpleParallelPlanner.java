package edu.kit.aquaplanning.aquaplanning;

import java.io.File;
import java.io.IOException;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.grounding.Grounder;
import edu.kit.aquaplanning.grounding.PlanningGraphGrounder;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.lifted.PlanningProblem;
import edu.kit.aquaplanning.parsing.ProblemParser;
import edu.kit.aquaplanning.planning.Planner;
import edu.kit.aquaplanning.planning.SimpleParallelPlanner;
import edu.kit.aquaplanning.validation.Validator;
import junit.framework.TestCase;

public class TestSimpleParallelPlanner extends TestCase {

	public void testSimpleParallelPlanner() throws IOException {
		Configuration config = new Configuration();
		config.numThreads = 8;
		config.searchTimeSeconds = 10;
		SimpleParallelPlanner spp = new SimpleParallelPlanner(config);
		File benchdir = new File("benchmarks");
		for (File domdir : benchdir.listFiles()) {
			String domain = domdir.getCanonicalPath() + "/domain.pddl";
			for (File f : domdir.listFiles()) {
				if (f.getName().startsWith("p") && f.getName().endsWith(".pddl")) {
					String problem = f.getCanonicalPath();
					testPlannerOnBenchmark(spp, domain, problem);
					break;
				}
			}
			break;
		}
	}
	
	private void testPlannerOnBenchmark(Planner planner, String domain, String problem) throws IOException {
		System.out.println("Testing planner on " + domain + ", " + problem);
		PlanningProblem pp = new ProblemParser().parse(domain, problem);
		Grounder grounder = new PlanningGraphGrounder(new Configuration());
		GroundPlanningProblem gpp = grounder.ground(pp);
		Plan p = planner.findPlan(gpp);
		if (p != null) {
			System.out.println(p);
			System.out.println("Plan is valid:" + Validator.planIsValid(gpp, p));
		} else {
			System.out.println("TIMEOUT");
		}
	}
}

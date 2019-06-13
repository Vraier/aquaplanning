package edu.kit.aquaplanning.aquaplanning;

import java.io.File;
import java.io.IOException;
import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.Configuration.CubeFinderMode;
import edu.kit.aquaplanning.Configuration.CutOffHeuristic;
import edu.kit.aquaplanning.Configuration.HeuristicType;
import edu.kit.aquaplanning.Configuration.PlannerType;
import edu.kit.aquaplanning.Configuration.SchedulerMode;
import edu.kit.aquaplanning.grounding.Grounder;
import edu.kit.aquaplanning.grounding.PlanningGraphGrounder;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.lifted.PlanningProblem;
import edu.kit.aquaplanning.parsing.ProblemParser;
import edu.kit.aquaplanning.planning.Planner;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy;
import edu.kit.aquaplanning.validation.Validator;
import junit.framework.TestCase;

public class TestSimpleParallelPlanner extends TestCase {

	public void testSimpleParallelPlanner() throws IOException {
		Configuration config = new Configuration();
		config.verbosityLevel = 2;
		//config.maxTimeSeconds = 10;
		
		config.plannerType = PlannerType.cubePlanner;
		config.numThreads = 4;
		config.numCubes = 100000;
		config.cubeFinderMode = CubeFinderMode.forwardSearch;
		config.cubeFindSearchStrategy = SearchStrategy.Mode.bestFirst;
		config.cubeFindHeuristic = HeuristicType.ffTrautmann;
		config.cubeSolveHeuristicWeight = 10;
		//config.cubeFinderMode = CubeFinderMode.backwardSearch;
		//config.cubeFindSearchStrategy = SearchStrategy.Mode.bestFirst;
		//config.cubeFindHeuristic = HeuristicType.relaxedPathLength;
		//config.cubeFindHeuristicWeight = 10;
		config.cutOffHeuristic = CutOffHeuristic.none;
		
		config.schedulerMode = SchedulerMode.roundRobin;
		config.schedulerTime = 1000;
		config.cubeSolveSearchStrategy = SearchStrategy.Mode.bestFirst;
		config.cubeSolveHeuristic = HeuristicType.ffTrautmann;
		config.cubeSolveHeuristicWeight = 10;
		
		Planner spp = Planner.getPlanner(config);
		
		//File benchdir = new File("testfiles");
		File benchdir = new File("benchmarks");
		for (File domdir : benchdir.listFiles()) {
			if(!domdir.getName().equals("Barman")) {
				continue;
			}
			String domain = domdir.getCanonicalPath() + "/domain.pddl";
			File testFile = new File(domain);
			if(!testFile.exists()) {
				continue;
			}
			for (File f : domdir.listFiles()) {
				if (f.getName().startsWith("p") && f.getName().endsWith(".pddl")) {
					String problem = f.getCanonicalPath();
					testPlannerOnBenchmark(spp, domain, problem);
					//break;
				}
			}
			//break;
		}
	}

	private void testPlannerOnBenchmark(Planner planner, String domain, String problem) throws IOException {
		System.out.println("Testing planner on " + domain + ", " + problem);
		PlanningProblem pp = new ProblemParser().parse(domain, problem);
		Grounder grounder = new PlanningGraphGrounder(new Configuration());
		GroundPlanningProblem gpp = grounder.ground(pp);
		Plan p = planner.findPlan(gpp);
		if (p != null) {
			//System.out.println(p);
			System.out.println("Plan is valid:" + Validator.planIsValid(gpp, p));
		} else {
			System.out.println("TIMEOUT");
		}
	}
}

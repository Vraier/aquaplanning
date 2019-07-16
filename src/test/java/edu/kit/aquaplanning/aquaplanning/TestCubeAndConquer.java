package edu.kit.aquaplanning.aquaplanning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.Configuration.CubeFinderMode;
import edu.kit.aquaplanning.Configuration.HeuristicType;
import edu.kit.aquaplanning.Configuration.PlannerType;
import edu.kit.aquaplanning.Configuration.SchedulerMode;
import edu.kit.aquaplanning.grounding.Grounder;
import edu.kit.aquaplanning.grounding.PlanningGraphGrounder;
import edu.kit.aquaplanning.model.cube.Cube;
import edu.kit.aquaplanning.model.ground.GroundPlanningProblem;
import edu.kit.aquaplanning.model.ground.Plan;
import edu.kit.aquaplanning.model.lifted.PlanningProblem;
import edu.kit.aquaplanning.parsing.ProblemParser;
import edu.kit.aquaplanning.planning.Planner;
import edu.kit.aquaplanning.planning.cube.finder.ForwardSearchCubeFinder;
import edu.kit.aquaplanning.planning.cube.finder.GenericCubeFinder;
import edu.kit.aquaplanning.planning.datastructures.SearchStrategy;
import edu.kit.aquaplanning.validation.Validator;
import junit.framework.TestCase;

public class TestCubeAndConquer extends TestCase {

	public static final String[] DEFAULT_TEST_DOMAINS = { "barman", "rover", "childsnack", "gripper", "zenotravel",
			"nurikabe", "petrinetalignment", "settlers", "GED", "floortile" };
	public static final String[] ADL_TEST_DOMAINS = { "openstacks" };

	private PlanningProblem pp;
	private GroundPlanningProblem gpp;

	public void testDefaultCubeAndCounquer() throws FileNotFoundException, IOException {

		assertTrue("Ignore this.", false);
		Configuration config = getDefaultConfig();

		for (String domain : DEFAULT_TEST_DOMAINS) {
			fullTest("testfiles/" + domain + "/domain.pddl", "testfiles/" + domain + "/p01.pddl", config);
		}
	}

	public void testCubeFinderModes() throws FileNotFoundException, IOException {

		assertTrue("Backward Cube Finding not working currently.", false);
		for (CubeFinderMode mode : CubeFinderMode.values()) {

			Configuration config = getDefaultConfig();
			config.cubeFinderMode = mode;

			for (String domain : DEFAULT_TEST_DOMAINS) {
				fullTest("testfiles/" + domain + "/domain.pddl", "testfiles/" + domain + "/p01.pddl", config);
			}
		}
	}

	public void testCutOffHeuristic() throws FileNotFoundException, IOException {

		assertTrue("Ignore this.", false);
		Configuration config = getDefaultConfig();
		config.numCubes = 5000;


		File benchdir = new File("benchmarks");
		for (File domdir : benchdir.listFiles()) {

			String domain = domdir.getCanonicalPath() + "/domain.pddl";
			File testFile = new File(domain);
			if (!testFile.exists()) {
				continue;
			}

			for (File f : domdir.listFiles()) {
				if (f.getName().startsWith("p") && f.getName().endsWith(".pddl")) {
					String problem = f.getCanonicalPath();

					System.out.println("Parsing ...");
					pp = new ProblemParser().parse(domain, problem);
					String out = pp.toString();
					assertTrue("String representation of problem is null", out != null);

					System.out.println("Grounding ...");
					Grounder grounder = new PlanningGraphGrounder(config);
					gpp = grounder.ground(pp);
					out = gpp.toString();

					System.out.println("Finding Cubes for Problem " + problem);
					GenericCubeFinder cFinder = new ForwardSearchCubeFinder(config);
					List<Cube> cubes = cFinder.findCubes(gpp, config.numCubes);
					//System.out.println("Generic Cube Finder found " + cFinder.totalFrontierSize + " cubes from which "
					//		+ cFinder.totalCutOffSize + " were cut off and " + cFinder.totalAnchorSize
					//		+ " were anchors.");

					Plan plan = cFinder.getPlan();
					if (plan != null) {
						System.out.println("Found a plan while searching for cubes.");
					} else {
						System.out.println("We have " + cubes.size() + " cubes.");
					}

					System.out.println("");
				}
			}
		}
	}

	public void testSchedulerModes() throws FileNotFoundException, IOException {

		//assertTrue("Ignore this.", false);
		for (SchedulerMode mode : SchedulerMode.values()) {

			if (mode != SchedulerMode.bandit) continue;
			Configuration config = getDefaultConfig();
			config.schedulerMode = mode;
			
			config.numThreads = 2;
			config.schedulerTime = 200;
			config.numCubes = 8;

			for (String domain : DEFAULT_TEST_DOMAINS) {
				fullTest("testfiles/" + domain + "/domain.pddl", "testfiles/" + domain + "/p01.pddl", config);
			}
		}
	}

	private void fullTest(String domainFile, String problemFile, Configuration config)
			throws FileNotFoundException, IOException {

		fullTest(domainFile, problemFile, config, 1, Integer.MAX_VALUE);
	}

	private void fullTest(String domainFile, String problemFile, Configuration config, int minPlanLength,
			int maxPlanLength) throws FileNotFoundException, IOException {

		System.out.println("Testing domain \"" + domainFile + "\", problem \"" + problemFile + "\".");

		System.out.println("Parsing ...");
		pp = new ProblemParser().parse(domainFile, problemFile);
		String out = pp.toString();
		assertTrue("String representation of problem is null", out != null);

		System.out.println("Grounding ...");
		Grounder grounder = new PlanningGraphGrounder(config);
		gpp = grounder.ground(pp);
		out = gpp.toString();
		assertTrue("String representation of ground problem is null", out != null);

		assertTrue("No actions have been produced during grounding.", gpp.getActions().size() > 0);

		System.out.println("Planning ...");
		System.out.println("Cube finding mode is: " + config.cubeFinderMode + ", Scheduler mode is: "
				+ config.schedulerMode + ".");
		Planner planner = Planner.getPlanner(config);
		Plan plan = planner.findPlan(gpp);

		System.out.println(plan);

		assertTrue("No plan has been found.", plan != null);

		assertTrue("The produced plan of length " + plan.getLength()
				+ " is shorter than the minimum valid plan length (" + minPlanLength + ").",
				plan.getLength() >= minPlanLength);
		assertTrue("The produced plan of length " + plan.getLength() + " is larger than the maximum valid plan length ("
				+ maxPlanLength + ").", plan.getLength() <= maxPlanLength);

		assertTrue("The produced plan is invalid.", Validator.planIsValid(gpp, plan));
		System.out.println("Done.\n");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		new File("_tmp_plan.txt").delete();
	}

	private Configuration getDefaultConfig() {

		// TODO: these are not the default settings
		Configuration config = new Configuration();

		config.seed = 1337;
		config.plannerType = PlannerType.cubePlanner;
		config.numThreads = 4;
		config.maxTimeSeconds = 300;

		config.numCubes = 10000;
		config.cubeFinderMode = CubeFinderMode.forwardSearch;
		config.cubeFindSearchStrategy = SearchStrategy.Mode.bestFirst;
		config.cubeFindHeuristic = HeuristicType.ffTrautmann;
		config.cubeSolveHeuristicWeight = 10;

		config.schedulerMode = SchedulerMode.bandit;
		config.schedulerTime = 4000;
		config.schedulerGrowth = 1.5;
		config.schedulerHillClimb = 0.5;

		config.cubeSolveSearchStrategy = SearchStrategy.Mode.bestFirst;
		config.cubeSolveHeuristic = HeuristicType.ffTrautmann;
		config.cubeSolveHeuristicWeight = 10;

		return config;
	}
}

package edu.kit.aquaplanning.aquaplanning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.kit.aquaplanning.Configuration;
import edu.kit.aquaplanning.Configuration.CubeFinderMode;
import edu.kit.aquaplanning.Configuration.CubeNodeType;
import edu.kit.aquaplanning.Configuration.HeuristicType;
import edu.kit.aquaplanning.Configuration.PlannerType;
import edu.kit.aquaplanning.Configuration.SchedulerMode;
import edu.kit.aquaplanning.Configuration.SolveMode;
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

		assertTrue("Ignore this.", false);
		for (CubeFinderMode mode : CubeFinderMode.values()) {

			if (mode != CubeFinderMode.forwardSearch)
				continue;
			Configuration config = getDefaultConfig();
			config.cubeFinderMode = mode;
			config.numCubes = 50;

			for (String domain : DEFAULT_TEST_DOMAINS) {
				fullTest("testfiles/" + domain + "/domain.pddl", "testfiles/" + domain + "/p01.pddl", config);
			}
		}
	}

	public void testSchedulerModes() throws FileNotFoundException, IOException {

		//assertTrue("Ignore this.", false);
		for (SchedulerMode mode : SchedulerMode.values()) {

			if (mode != SchedulerMode.roundRobin)
				continue;
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

	public void testOpenClosedNodes() throws FileNotFoundException, IOException {

		assertTrue("Ignore this.", false);
		for (CubeNodeType type : CubeNodeType.values()) {

			Configuration config = getDefaultConfig();
			config.cubeNodeType = type;
			config.numThreads = 2;
			config.schedulerTime = 200;
			config.numCubes = 40;

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
				+ config.schedulerMode + ", node Type is: " + config.cubeNodeType + ".");
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

	private Configuration getDefaultConfig() {

		// these are not necessarily the default settings
		Configuration config = new Configuration();

		config.problemFile = "testFile";
		config.csvOutputFolder = "testOutput";
		config.seed = 1337;
		config.plannerType = PlannerType.cubePlanner;
		config.numThreads = 4;
		config.maxTimeSeconds = 300;

		config.numCubes = 100;
		config.shareVisitedStates = true;
		config.cubeFinderMode = CubeFinderMode.forwardSearch;
		config.cubeNodeType = CubeNodeType.closed;
		config.cubePercent = 0.3;
		config.cubeSparseInterval = 3;
		config.cubeFindDescents = 5;
		//config.cubeFindSearchStrategy = SearchStrategy.Mode.bestFirst;
		config.cubeFindSearchStrategy = SearchStrategy.Mode.breadthFirst;
		config.cubeFindHeuristic = HeuristicType.ffTrautmann;
		config.cubeFindHeuristicWeight = 10;
		config.cutOffAnchors = 10;
		config.cutOffDistanceRatio = 0.3;

		config.schedulerMode = SchedulerMode.greedyBandit;
		config.schedulerIterations = 0;
		config.schedulerTime = 4000;
		config.schedulerGrowth = 1.5;
		config.schedulerHillClimb = 0.5;

		config.solveMode = SolveMode.forwardSearch;
		config.cubeSolveSearchStrategy = SearchStrategy.Mode.bestFirst;
		config.cubeSolveHeuristic = HeuristicType.ffTrautmann;
		config.cubeSolveHeuristicWeight = 10;

		return config;
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		new File("_tmp_plan.txt").delete();
	}
}

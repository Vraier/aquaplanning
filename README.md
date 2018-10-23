# Aquaplanning – QUick Automated Planning

This is a Java framework for Automated Planning, developed by Tomáš Balyo and Dominik Schreiber for the lecture _Automated Planning and Scheduling_ at Karlsruhe Institute of Technology (KIT). It is meant as a simple, but extensible and reasonably powerful planning environment for PDDL problems, for educational and any other means.

## Features

Aquaplanning supports PDDL (Planning Domain Description Language) files as an input for planning domains and problems. The following features are implemented as of now (i.e. problems with these features can be parsed and grounded):

* Basic STRIPS planning with typing
* Negative goals
* Equality (as a predicate "=" for objects)
* Conditional effects
* Universal quantifications for preconditions, effects, and goals
    - Conditional effects and quantifications cannot be used in a nested way right now.
* Action costs (in its basic form, with constant cost per operator)

For planning problems using these features (or any subset), a full representation of the read problem is available in the form of Java objects after parsing, as well as a separate representation after grounding the problem.

Currently, Aquaplanning features a trivial (but complete) grounding procedure and an equally trivial forward search planner. Better algorithms will be added. At the end of the planning pipeline, a tiny plan validator can be employed to ensure the planner's correctness.

## Building and installing

The framework is built using Maven. E.g. using Eclipse, the project can be directly imported as a Maven project and then built and/or installed. The framework is written from scratch and only depends on antlr4 (for the parsing of PDDL files) and JUnit for tests. Maven should take care of these dependencies.

## Usage

Aquaplanning can be used as an off-the-shelf planner; you can specify a domain file and a problem file as arguments (in that order), and it will attempt to parse, ground, and solve the problem. You can try the files provided in the `testfiles/` directory.

When you want to use your own planner, implement the Planner interface and take a look at the DefaultPlanner class as a point of reference. Same holds for custom grounding procedures (Grounder, DefaultGrounder).

If you find any bugs or you consider something to be missing, please let us know. We appreciate receiving issues and/or pull requests!
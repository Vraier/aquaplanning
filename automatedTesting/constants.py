import os
import testUtil as util

timeLimit = 5000 # Time after we timeOut in seconds

# Strings and Paths
breakSequenze = '###############################################################\n'
relativeJarPath = '../target/aquaplanning-0.0.1-SNAPSHOT-jar-with-dependencies.jar'
relativeBenchmarkPath = '../benchmarks'
domainFile = 'domain.pddl'
outpuFile = 'output1.txt'
homeDirName = os.path.dirname(__file__)

outputFilePath = os.path.join(homeDirName, outpuFile)
jarPath = os.path.join(homeDirName, relativeJarPath)
benchmarkPath = os.path.join(homeDirName, relativeBenchmarkPath)

# Command Arguments for testCombinations
maxSeconds = ['-t='+str(timeLimit)]
plannerType = ['-p=cubePlanner']
numThreads = ['-T=8']
verbosityLevel = ['-v=2']
#numCubes = ['-c=1', '-c=48', '-c=1000', '-c=100000']
numCubes = ['-c=2000']
cubeFinderMode = ['--cubeFinder=forwardSearch'] # '--cubeFinder=backwardSearch'
cubeFindSearchStrategy = ['-cfs=bestFirst'] # '-cfs=breadthFirst' 
schedulerMode = ['-sched=greedyBandit'] #['-sched=bandit', -sched=exponential', '-sched=hillClimbing', '-sched=roundRobin']
schedulerTime = ['-schedT=4000', '-schedT=1000','-schedT=400', '-schedT=100']
exponentialGrowth = ['-schedExpG=2']
#hillClimbPercent = ['-schedHill=0.8', '-schedHill=0.5' , '-schedHill=0.25', '-schedHill=0.1']


commandLists = [maxSeconds, plannerType, numThreads, verbosityLevel, numCubes, cubeFinderMode, cubeFindSearchStrategy, schedulerMode, schedulerTime, exponentialGrowth]

# for testSingle
#arguments = [['-t=5000', '-H=ffWilliams']]
arguments = util.listCombinations(commandLists)
outputFolder = 'FullGreedyBanditTest'
outputFolderPath = os.path.join(homeDirName, outputFolder)
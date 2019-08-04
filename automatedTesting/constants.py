import os
import testUtil as util

timeLimit = 1000 # Time after we timeOut in seconds

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
numCubes = ['-c=8000', '-c=2000', '-c=800', '-c=80']
#numCubes = ['-c=2000', '-c=800']
shareVisitedStates = ['-svs']
#cubeFindDescents = ['-csd=1', '-csd=5', '-csd=20']
#cubeInterval = ['-csi=1', '-csi=10', '-csi=100']
#cubePercent = ['-cp=1.0', '-cp=0.1', '-cp=0.01']
cubeNodeType = ['-cnt=closed']
#cubeFinderMode = ['--cubeFinder=portfolio', '--cubeFinder=randomGreedy'] # '--cubeFinder=backwardSearch'
cubeFinderMode = ['--cubeFinder=forwardSearch']
cubeFindSearchStrategy = ['-cfs=bestFirst'] # '-cfs=breadthFirst' 
schedulerMode = ['-sched=greedyBandit'] #['-sched=bandit', -sched=exponential', '-sched=hillClimbing', '-sched=roundRobin']
schedulerTime = ['-schedT=100']
exponentialGrowth = ['-schedExpG=2']
#hillClimbPercent = ['-schedHill=0.8', '-schedHill=0.5' , '-schedHill=0.25', '-schedHill=0.1']


commandLists = [maxSeconds, plannerType, numThreads, verbosityLevel, numCubes, shareVisitedStates, cubeNodeType, cubeFinderMode, cubeFindSearchStrategy, schedulerMode, schedulerTime, exponentialGrowth]

# for testSingle
#arguments = [['-t=5000', '-H=ffWilliams']]
arguments = util.listCombinations(commandLists)
outputFolder = 'ShareVisitedStates'
outputFolderPath = os.path.join(homeDirName, outputFolder)
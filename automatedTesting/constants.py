import os
import testUtil as util

# Variable Strings
outputFolder = 'ttDOSTUFFHEREEEEEEEEE'

# Constant strings and paths
breakSequenze = '###############################################################\n'
relativeJarPath = '../target/aquaplanning-0.0.1-SNAPSHOT-jar-with-dependencies.jar'
relativeBenchmarkPath = '../benchmarks'
domainFile = 'domain.pddl'
outpuFile = 'output1.txt'
homeDirName = os.path.dirname(__file__)
outputFilePath = os.path.join(homeDirName, outpuFile)
jarPath = os.path.join(homeDirName, relativeJarPath)
benchmarkPath = os.path.join(homeDirName, relativeBenchmarkPath)
outputFolderPath = os.path.join(homeDirName, outputFolder)

# Command Arguments for testCombinations
timeLimit = 2000 # Time after we timeOut in seconds for parallel test
maxSeconds = ['-t='+str(timeLimit)]
plannerType = ['-p=cubePlanner']
numThreads = ['-T=8']
verbosityLevel = ['-v=2']
#numCubes = ['-c=8000', '-c=2000', '-c=800', '-c=80']
numCubes = ['-c=6000', '-c=600', '-c=60']
#shareVisitedStates = ['-svs']
#cubeFindDescents = ['-csd=1', '-csd=5', '-csd=20']
#cubeInterval = ['-csi=1', '-csi=10', '-csi=100']
#cubePercent = ['-cp=1.0', '-cp=0.1', '-cp=0.01']
cubeNodeType = ['-cnt=open'] #'-cnt=closed'
#cubeFinderMode = ['--cubeFinder=portfolio', '--cubeFinder=randomGreedy'] # '--cubeFinder=backwardSearch'
#cubeFinderMode = ['--cubeFinder=randomBestFirst']
cubeFinderMode = ['--cubeFinder=forwardSearch']
cubeFindSearchStrategy = ['-cfs=breadthFirst'] # '-cfs=breadthFirst' '-cfs=bestFirst'
schedulerMode = ['-sched=roundRobin'] #['-sched=bandit', -sched=exponential', '-sched=hillClimbing', '-sched=roundRobin', '-sched=greedyBandit']
schedulerTime = ['-schedT=1000', '-schedT=100', '-schedT=10']
#exponentialGrowth = ['-schedExpG=2']
#hillClimbPercent = ['-schedHill=0.8', '-schedHill=0.5' , '-schedHill=0.25', '-schedHill=0.1']

commandLists = [maxSeconds, plannerType, numThreads, verbosityLevel, numCubes, cubeNodeType, cubeFinderMode, cubeFindSearchStrategy, schedulerMode, schedulerTime]
arguments = util.listCombinations(commandLists)


# for testSingle
#arguments = [['-t=5000', '-H=ffWilliams']]
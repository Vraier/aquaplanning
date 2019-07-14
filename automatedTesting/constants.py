import os

timeLimit = 300 # Time after we timeOut in seconds

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

# for testSingle
arguments = ['-t=5000', '-H=ffWilliams']
outputFolder = 'result'

outputFolderPath = os.path.join(homeDirName, outputFolder)

# Command Arguments
maxSeconds = ['-t='+str(timeLimit)]
plannerType = ['-p=cubePlanner']
numThreads = ['-T=64']
verbosityLevel = ['-v=2']
#numCubes = ['-c=1', '-c=48', '-c=1000', '-c=100000']
numCubes = ['-c=20000']
cubeFinderMode = ['--cubeFinder=forwardSearch'] # '--cubeFinder=backwardSearch'
cubeFindSearchStrategy = ['-cfs=bestFirst'] # '-cfs=breadthFirst' 
schedulerMode = ['-sched=hillClimbing'] #['-sched=exponential', '-sched=bandit', '-sched=roundRobin']
schedulerTime = ['-schedT=2000']
exponentialGrowth = ['-schedExpG=2']
hillClimbPercent = ['-schedHill=0.8', '-schedHill=0.5' , '-schedHill=0.25', '-schedHill=0.1']


commandLists = [maxSeconds, plannerType, numThreads, verbosityLevel, numCubes, cubeFinderMode, cubeFindSearchStrategy, schedulerMode, schedulerTime, exponentialGrowth, hillClimbPercent]
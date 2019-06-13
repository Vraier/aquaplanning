import os
import subprocess
import testUtil as util
import constants as consts


# Strings and Paths
breakSequenze = '###############################################################\n'
relativeJarPath = '../target/aquaplanning-0.0.1-SNAPSHOT-jar-with-dependencies.jar'
relativeBenchmarkPath = '../benchmarks'
domainFile = 'domain.pddl'
outpuFile = 'output1.txt'
dirName = os.path.dirname(__file__)

# Command Arguments
maxSeconds = ['-t='+str(consts.timeLimit)]
plannerType = ['-p=cubePlanner']
numThreads = ['-T=48']
verbosityLevel = ['-v=2']
numCubes = ['-c=1', '-c=100', 'c=10000', '-c=1000000']
cubeFinderMode = ['--cubeFinder=forwardSearch'] #, '--cubeFinder=backwardSearch']
schedulerMode = ['-sched=exponential']
exponentialGrowth = ['-schedExpG=2']
cubeFindSearchStrategy = ['-cfs=breadthFirst']
cutOffHeuristic = ['-cut=none']

commandLists = [maxSeconds, plannerType, numThreads, verbosityLevel, numCubes, cubeFinderMode, schedulerMode, exponentialGrowth, cubeFindSearchStrategy, cutOffHeuristic]
commandArguments = util.listCombinations(commandLists)

outputPath = os.path.join(dirName, outpuFile)
jarPath = os.path.join(dirName, relativeJarPath)
benchmarkPath = os.path.join(dirName, relativeBenchmarkPath)

commandList = []
commandPrefix = ['java', '-jar', jarPath]
commandProblems = []

print("We have ", len(commandArguments), " argument combinations.")

for folder in os.listdir(benchmarkPath):

    #if(folder != 'Rover'):
    #    continue
    currentBenchmarkPath = os.path.join(benchmarkPath, folder)
    if(not util.hasDomain(currentBenchmarkPath)):
        continue

    for filename in os.listdir(currentBenchmarkPath):
        if (filename != domainFile and filename.endswith('.pddl')):
            localCommandProblems = []
            domainPath = os.path.join(currentBenchmarkPath, domainFile)
            problemPath = os.path.join(currentBenchmarkPath, filename)
            localCommandProblems.append(domainPath)
            localCommandProblems.append(problemPath)
            commandProblems.append(localCommandProblems)

# Returns a list with all combinations of problems and arguments
# Starts with the first problem and appends all aguments then takes the seconde problem etc..
for problem in commandProblems:
    for argument in commandArguments:
        commandList.append(commandPrefix + problem + argument)

with open(outputPath, 'a') as outputFile:
    outputFile.flush()
    for command in commandList:
        commandName = ', '.join(command)
        commandName += '\n'
        print('Working on Command: ' + commandName)
        outputFile.write(breakSequenze)
        outputFile.write(commandName)
        outputFile.flush()
        subprocess.call(command, stdout=outputFile, stderr=outputFile)
        outputFile.flush()

import os
import subprocess
import testUtil as util


# Strings and Paths
breakSequenze = '###############################################################\n'
relativeJarPath = '../target/aquaplanning-0.0.1-SNAPSHOT-jar-with-dependencies.jar'
relativeBenchmarkPath = '../benchmarks'
domainFile = 'domain.pddl'
dirName = os.path.dirname(__file__)

# Command Arguments
plannerType = ['-p=cubePlanner']
numThreads = ['-T=8']
verbosityLevel = ['-v=2']
numCubes = ['-c=1000']
cubeFinderMode = ['--cubeFinder=forwardSearch', '--cubeFinder=diverseSearch'] #, '--cubeFinder=backwardSearch']
schedulerMode = ['-sched=exponential']
exponentialGrowth = ['-schedExpG=1.5']
cubeFindSearchStrategy = ['-cfs=breadthFirst']

commandLists = [plannerType, numThreads, verbosityLevel, numCubes, cubeFinderMode, schedulerMode, exponentialGrowth, cubeFindSearchStrategy]
commandArguments = util.listCombinations(commandLists)

outputPath = os.path.join(dirName, 'output.txt')
jarPath = os.path.join(dirName, relativeJarPath)
benchmarkPath = os.path.join(dirName, relativeBenchmarkPath)

commandList = []
commandPrefix = ['java', '-jar', jarPath]
commandProblems = []

print("We have ", len(commandArguments), " argument combinations.")

for folder in os.listdir(benchmarkPath):

    if(folder != 'Rover'):
        continue
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
        outputFile.write(breakSequenze)
        outputFile.flush()
        subprocess.call(command, stdout=outputFile, stderr=outputFile)
        outputFile.flush()

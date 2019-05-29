import os
import subprocess
import testUtil as util


# Strings and Paths
breakSequenze = '###############################################################\n'
relativeJarPath = '../target/aquaplanning-0.0.1-SNAPSHOT-jar-with-dependencies.jar'
relativeBenchmarkPath = '../testfiles'
domainFile = 'domain.pddl'
dirName = os.path.dirname(__file__)

# Command Arguments
plannerType = ['-p=cubePlanner']
numThreads = ['-T=4']
verbosityLevel = ['-v=2']
numCubes = ['-c=200']
cubeFinderMode = ['--cubeFinder=forwardSearch', '--cubeFinder=backwardSearch']
schedulerMode = ['-sched=roundRobin']
cubeFindSearchStrategy = ['-cfs=breadthFirst']


commandLists = [plannerType, numThreads, verbosityLevel, numCubes, cubeFinderMode, schedulerMode, cubeFindSearchStrategy]

outputPath = os.path.join(dirName, 'output.txt')
jarPath = os.path.join(dirName, relativeJarPath)
benchmarkPath = os.path.join(dirName, relativeBenchmarkPath)

commandList = []
commandPrefix = ['java', '-jar', jarPath]
commandProblems = []
commandArguments = util.listCombinations(commandLists)

for folder in os.listdir(benchmarkPath):

    currentBenchmarkPath = os.path.join(benchmarkPath, folder)
    hasDomain = False
    for filename in os.listdir(currentBenchmarkPath):
        if (filename == domainFile):
            hasDomain = True

    if hasDomain:
        for filename in os.listdir(currentBenchmarkPath):
            if (not filename == domainFile and filename.endswith('.pddl')):
                localCommandProblems = []
                domainPath = os.path.join(currentBenchmarkPath, domainFile)
                problemPath = os.path.join(currentBenchmarkPath, filename)
                localCommandProblems.append(domainPath)
                localCommandProblems.append(problemPath)
                commandProblems.append(localCommandProblems)

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
        subprocess.call(command, stdout=outputFile, stderr=outputFile, shell=True)
        outputFile.flush()

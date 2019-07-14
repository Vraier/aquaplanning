import os
import time
import subprocess
import testUtil as util
import constants as consts

commandArguments = util.listCombinations(consts.commandLists)

commandList = []
commandPrefix = ['java', '-jar', consts.jarPath]
commandProblems = []

print("We have " + str(len(commandArguments)) + " argument combinations.")

for folder in os.listdir(consts.benchmarkPath):

    currentBenchmarkPath = os.path.join(consts.benchmarkPath, folder)
    if(not util.hasDomain(currentBenchmarkPath)):
        continue

    for filename in os.listdir(currentBenchmarkPath):
        if (filename != consts.domainFile and filename.endswith('.pddl')):
            localCommandProblems = []
            domainPath = os.path.join(currentBenchmarkPath, consts.domainFile)
            problemPath = os.path.join(currentBenchmarkPath, filename)
            localCommandProblems.append(domainPath)
            localCommandProblems.append(problemPath)
            commandProblems.append(localCommandProblems)

# Returns a list with all combinations of problems and arguments
# Starts with the first problem and appends all aguments then takes the seconde problem etc..
for problem in commandProblems:
    for argument in commandArguments:
        commandList.append(commandPrefix + problem + argument)

globalStart = time.time()
with open(consts.outputFilePath, 'a') as outputFile:
    outputFile.flush()
    for command in commandList:
        commandName = ' '.join(command)
        commandName += '\n'
        print('Working on Command: ' + commandName)
        localStart = time.time()
        outputFile.write(consts.breakSequenze)
        outputFile.write(commandName)
        outputFile.flush()
        subprocess.call(command, stdout=outputFile, stderr=outputFile)
        outputFile.flush()
        print('Finished command in ' + str(time.time()-localStart) + " seconds.")

print('Finished all commands in ' + str(time.time()-globalStart) + " seconds.")

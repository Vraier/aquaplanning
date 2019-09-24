import os
import sys
import time
import errno
import subprocess
import testUtil as util
import constants as consts

testFile = sys.argv[1]
(head, name) = os.path.split(testFile)
(_, localFolder) = os.path.split(head)
testDomain = os.path.join(head, 'domain.pddl')

testDomainPath = os.path.join(consts.homeDirName, testDomain)
testFilePath = os.path.join(consts.homeDirName, testFile)
outputFilePath = os.path.join(consts.homeDirName, consts.outputFolder, localFolder, name + '.txt')

commandPrefix = ['java', '-jar', consts.jarPath]
commandList = []
ArgumentNumber = 0
for command in consts.arguments:
    csvOutputFolderName = "-csvO=" + localFolder + "_" + name[:-5] + "_" + str(ArgumentNumber)
    ArgumentNumber = ArgumentNumber + 1
    commandList.append(commandPrefix + [testDomain] + [testFile] + [csvOutputFolderName] + command)

print('We have ' + str(len(commandList))+ ' argument Combinations.')
for command in commandList:
    if not os.path.exists(os.path.dirname(outputFilePath)):
        try:
            os.makedirs(os.path.dirname(outputFilePath))
        except OSError as exc: # Guard against race condition
            if exc.errno != errno.EEXIST:
                raise
            
    with open(outputFilePath, 'a') as outputFile:
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
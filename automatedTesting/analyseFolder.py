import os
import re
import analyseUtil as util
import constants as const
import matplotlib.pyplot as plt

numTester = 4
testFolder = 'resultBandit2' #'compareForwardBackwardGripper.txt'
homeDirName = os.path.dirname(__file__)
folderPath =  os.path.join(homeDirName, testFolder)

groupedTime = []
for _ in range(numTester):
    groupedTime.append([])

for folder in os.listdir(folderPath):
    currPath = os.path.join(folderPath, folder)
    for fileName in os.listdir(currPath):
        filePath = os.path.join(currPath, fileName)

        with open(filePath, 'r') as analyseFile:
            blocks = util.splitResults(analyseFile)

            # fill groupedTime with data
            for x in range(numTester):
                if(not util.foundValidPlan(blocks[x])):
                    #print('got timeout at block' + str(x))
                    groupedTime[x].append(const.timeLimit * 10)
                else:
                    groupedTime[x].append(util.getCalcTime(blocks[x]))


for x in range(numTester):
    plt.semilogy(sorted(groupedTime[x]), marker = '.', linestyle = 'None', label = x)

#plt.semilogy(evenTime, color = 'b', marker = '.', linestyle = 'None', label = 'Parallel')
#plt.semilogy(unevenTime, color = 'r', marker = '.', linestyle = 'None', label = 'Sequential')
plt.xlabel('Number of testcase')
plt.ylabel('Time in seconds')
plt.title('Comparing different approaches to planning')
plt.legend()
plt.show()
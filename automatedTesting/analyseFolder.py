import os
import re
import analyseUtil as util
import constants as const
import matplotlib.pyplot as plt

def getCompTimes(testFolder, numTester):
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
                        groupedTime[x].append(const.timeLimit * 5.1)
                    else:
                        groupedTime[x].append(util.getCalcTime(blocks[x]))
    return groupedTime

resultSE = getCompTimes('resultSequential', 1)
resultFGB = getCompTimes('FullGreedyBanditTest', 4)
if len(resultSE[0]) != len(resultFGB[0]): print("Error")
speedUp = [s/p for (s,p) in zip(sorted(resultSE[0]), sorted(resultFGB[3]))]

resultB1 = getCompTimes('resultBandit', 4)
resultB2 = getCompTimes('resultBandit2', 4)
resultGB = getCompTimes('resultGreedyBandit', 4)
resultGBCA = getCompTimes('GreedyBanditCubeAmountTest', 4)
resultFI = getCompTimes('resultForcedImprovement', 4)

#showResult = [resultGB[3], resultGBCA[2], resultGBCA[3]]
showResult = resultGBCA





for x in range(len(showResult)):
    plt.semilogy(sorted(showResult[x]), marker = '.', linestyle = 'None', label = x)

#plt.semilogy(evenTime, color = 'b', marker = '.', linestyle = 'None', label = 'Parallel')
#plt.semilogy(unevenTime, color = 'r', marker = '.', linestyle = 'None', label = 'Sequential')
plt.xlabel('Number of testcase')
plt.ylabel('Time in seconds')
plt.title('Comparing different approaches to planning')
plt.legend()
plt.show()
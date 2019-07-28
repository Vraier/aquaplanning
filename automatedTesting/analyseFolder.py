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
                        #continue
                        groupedTime[x].append(const.timeLimit * 5.1)
                    else:
                        groupedTime[x].append(util.getCalcTime(blocks[x]))

    for x in range(numTester):
        groupedTime[x] = sorted(groupedTime[x])
    return groupedTime

def plotCompTimes(listsToShow):
    for x in range(len(showResult)):
        plt.plot(showResult[x], marker = '.', linestyle = '-', label = x)
    plt.xlabel('Test file instance')
    plt.ylabel('Time in seconds (logarithmic scale)')
    plt.yscale("symlog")
    plt.title('Computation time on solving different test instances sorted by time')
    plt.legend()
    plt.show()

def plotSpeedUp(list1, list2):
    speedUp = [s/p for (s,p) in zip(list1, list2)]
    plt.plot(speedUp, marker = '.', linestyle = 'none')

    plt.axhline(y=1, color='black', linestyle='-')
    plt.axhline(y=2, color='black', linestyle='-')
    plt.axhline(y=4, color='black', linestyle='-')

    plt.xlabel('Test file instance')
    plt.ylabel('Speedup t_1/t_2')
    plt.title('Speedup relative to two different solver instances')
    plt.show()

resultSE = getCompTimes('resultSequential', 1)
resultFGB = getCompTimes('FullGreedyBanditTest', 4)

resultB1 = getCompTimes('resultBandit', 4)
resultB2 = getCompTimes('resultBandit2', 4)
resultGB = getCompTimes('resultGreedyBandit', 4)
resultGBCA = getCompTimes('GreedyBanditCubeAmountTest', 4)
resultFI = getCompTimes('resultForcedImprovement', 4)
resultNTC = getCompTimes('NodeTypeComparison', 2)

#showResult = [resultGBCA[1], resultGBCA[2], resultGBCA[3], resultNTC[1]]
#showResult = resultSE + resultFGB
showResult = resultGBCA

plotCompTimes(showResult)
#plotSpeedUp(resultNTC[0], resultNTC[1])



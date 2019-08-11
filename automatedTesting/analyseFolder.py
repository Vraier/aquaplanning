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
                        groupedTime[x].append(const.timeLimit * 10)
                    else:
                        groupedTime[x].append(util.getCalcTime(blocks[x]))

    for x in range(numTester):
        groupedTime[x] = sorted(groupedTime[x])
    return groupedTime

def plotCompTimes(listsToShow):
    markers = ['.', '+', 'x', '1', '2', '3', '4']
    for x in range(len(showResult)):
        plt.plot(showResult[x], marker = markers[x], linestyle = '-', label = x)
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
resultBSE = getCompTimes('bigSequentialTest', 1)
resultFGB = getCompTimes('FullGreedyBanditTest', 4)

resultSSE = getCompTimes('smallSequentialTest', 1)
#resultB1 = getCompTimes('resultBandit', 4)
#resultB2 = getCompTimes('resultBandit2', 4)
#resultGB = getCompTimes('resultGreedyBandit', 4)
#resultGBCA = getCompTimes('GreedyBanditCubeAmountTest', 4)
#resultFI = getCompTimes('resultForcedImprovement', 4)
resultNTC = getCompTimes('NodeTypeComparison', 2)
resultSP = getCompTimes('SparseTest', 6)
resultRSP = getCompTimes('CubeRandomSparseTest', 6)
resultPARG = getCompTimes('CubeFindPortfolioAndRandom', 8)
resultSVS = getCompTimes('ShareVisitedStates', 4)


#showResult = resultFGB # Different sched Time intervals -schedT=4000, 1000, 400, 100
#showResult = resultSE + [resultFGB[3]] # compare sequential with the greedy bandit approach
#plotSpeedUp(resultSE[0], resultFGB[3]) # look at speedup for sequential and parallel

#showResult = resultGBCA # -c=8000, 2000, 800, 80
#showResult = resultSSE + [resultSP[4]]
#showResult = resultNTC + [resultGBCA[3]] # nodes open and closed (-c=2000) and best cube amount open (-c=80)
#plotSpeedUp(resultNTC[0], resultNTC[1]) # look at speedup for open and closed nodes
#showResult = [resultSP[0]] + [resultNTC[1]] #sanity check with node type closed
#showResult = resultSP[0:3] # -c=2000, 200, 20 -interval=1, 10, 100
#showResult = resultSP[3:6] # -c=80 -interval=1, 10, 100
#showResult = [resultSP[1]] + [resultSP[4]] # compare -c=200, 80 and -interval=10

showResult = resultSE + resultBSE + [resultFGB[3]] #we didnt get twice as fast but made the sequential one twice as slow
showResult = [resultPARG[0], resultPARG[4]] + resultSSE # Portfolio with 2000 and 800 Cubes
showResult = resultPARG[1:4] + resultPARG[5:8] + resultSSE # randomGreedy with 2000 cubes and 1, 5, 20 descents and 800 cubes
showResult = resultSP[0:3] + resultRSP[0:3] # normal forward search and taking random x% of cubes
#showResult = resultSP[3:6] + resultRSP[3:6]
showResult =  [resultRSP[2]] + resultSSE # best finder so far?
#showResult = resultSVS # sharing visited States
showResult = resultSSE + [resultNTC[1]] + [resultSVS[1]] # comparing it with closed

plotCompTimes(showResult)



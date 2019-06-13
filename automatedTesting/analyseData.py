import os
import re
import analyseUtil as util
import constants as const
import matplotlib.pyplot as plt

numTester = 4
testFile = 'compareSequentialHeuristic.txt' #'compareForwardBackwardGripper.txt'
dirName = os.path.dirname(__file__)
filePath =  os.path.join(dirName, testFile)

blocks = []
groupedResults = []
groupedTime = []
with open(filePath, 'r') as analyseFile:
    blocks = util.splitResults(analyseFile)

for x in range(numTester):
    groupedResults.append(blocks[x::numTester])
    groupedTime.append([])

for x in range(numTester):
    for y in range(len(groupedResults[0])):
        #testNumber.append(util.getTestNumber(blockEven[x]))
        if(not util.foundValidPlan(groupedResults[x][y])):
            #print('got timeout at block' + str(x))
            groupedTime[x].append(const.timeLimit * 1.2)
        else:
            groupedTime[x].append(util.getCalcTime(groupedResults[x][y]))

#evenSorted = [y for x,y in sorted(zip(testNumber,evenTime))]
#unevenSorted = [y for x,y in sorted(zip(testNumber,unevenTime))]

for x in range(numTester):
    plt.semilogy(groupedTime[x], marker = '.', linestyle = 'None', label = x)

#plt.semilogy(evenTime, color = 'b', marker = '.', linestyle = 'None', label = 'Parallel')
#plt.semilogy(unevenTime, color = 'r', marker = '.', linestyle = 'None', label = 'Sequential')
plt.xlabel('Number of testcase')
plt.ylabel('Time in seconds')
plt.title('Comparing different approaches to planning')
plt.legend()
plt.show()
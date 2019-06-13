import os
import re
import analyseUtil as util
import constants as const
import matplotlib.pyplot as plt

testFile = 'output1.txt' #'compareForwardBackwardGripper.txt'
dirName = os.path.dirname(__file__)
filePath =  os.path.join(dirName, testFile)

with open(filePath, 'r') as analyseFile:
    blocks = util.splitResults(analyseFile)
    blockEven = blocks[0::2]
    blockUneven = blocks[1::2]

print(len(blocks))

testNumber = []
evenTime = []
unevenTime = []
for x in range(len(blockEven)):
    testNumber.append(util.getTestNumber(blockEven[x]))
    if(not util.foundValidPlan(blockEven[x])):
        #print('got timeout at block' + str(x))
        evenTime.append(const.timeLimit * 1.2)
    else:
        evenTime.append(util.getCalcTime(blockEven[x]))
    if(not util.foundValidPlan(blockUneven[x])):
        #print('got timeout at block' + str(x))
        unevenTime.append(const.timeLimit * 1.2)
    else:
        unevenTime.append(util.getCalcTime(blockUneven[x]))

evenSorted = [y for x,y in sorted(zip(testNumber,evenTime))]
unevenSorted = [y for x,y in sorted(zip(testNumber,unevenTime))]

plt.semilogy(evenTime, color = 'b', marker = '.', linestyle = 'None', label = 'Parallel')
plt.semilogy(unevenTime, color = 'r', marker = '.', linestyle = 'None', label = 'Sequential')
plt.xlabel('Number of testcase')
plt.ylabel('Time in seconds')
plt.title('Comparing forward and backward search')
plt.legend()
plt.show()
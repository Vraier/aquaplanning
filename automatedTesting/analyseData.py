import os
import re
import matplotlib.pyplot as plt

dirName = os.path.dirname(__file__)
filePath =  os.path.join(dirName, 'compareForwardBackwardGripper.txt')


arguments = []
numbers = []
with open(filePath, 'r') as analyseFile:
    for line in analyseFile:
        arguments += re.findall(r"java, -jar, .*$", line)
        numbers += re.findall(r"^.*Planner finished with a plan of length.*$", line)
# get string between [...] for every snd element
testNumber = [int(re.search(r"p[0-9]{2}\.pddl", x).group(0)[1:3]) for x in arguments[0::2]]
forward = [float(x[x.find("[")+1:x.find("]")]) for x in numbers[0::2]]
backward = [float(x[x.find("[")+1:x.find("]")]) for x in numbers[1::2]]

forwardSorted = [y for x,y in sorted(zip(testNumber,forward))]
backwardSorted = [y for x,y in sorted(zip(testNumber,backward))]

plt.semilogy(forwardSorted, color = 'b', marker = '.', linestyle = 'None', label = 'ForwardSearch')
plt.semilogy(backwardSorted, color = 'r', marker = '.', linestyle = 'None', label = 'BackwardSearch')
plt.xlabel('Number of testcase')
plt.ylabel('Time in seconds')
plt.title('Comparing forward and backward search')
plt.legend()
plt.show()
import os
import re

INF = 310.0

def splitResults(fileContent):
    lines = fileContent.readlines()
    results = []
    block = []
    for line in lines[1:]:
        if line.startswith('#######################'):
            results.append(block)
            block = []
        else:
            block.append(line)
    results.append(block)
    return results

def foundValidPlan(block):
    for line in block:
        if re.match(r"^.*Plan has been found to be valid.*$", line):
            return True
    return False

# TestFile must be numbered with two digites!
def getTestNumber(block):
    header = block[0]
    testNumber = int(re.search(r"p[0-9]{2}\.pddl", header).group(0)[1:3])
    #print(testNumber)
    return testNumber

def getCalcTime(block):
    result = INF
    for line in block:
        if re.match(r"^.*Planner finished with a plan of length.*$", line):
            result = float(line[line.find("[")+1:line.find("]")])
    #print(result)
    return result
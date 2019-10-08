import os
import re
import constants as consts

INF = consts.maxSeconds

def splitResults(fileContent):
    lines = fileContent.readlines()
    results = []
    block = []
    for line in lines[1:]:
        if line.startswith('####################'):
            results.append(block)
            block = []
        else:
            block.append(line)
    results.append(block)
    return results

def foundValidPlan(block):
    for line in block:
        if "Plan has been found to be valid" in line:
            return True
    return False

def foundPlanWhileCubing(block):
    for line in block:
        if "found a plan while searching for cubes" in line:
            return True
        return False

def getCalcTime(block):
    result = consts.timeLimit * 1.2
    for line in block:
        if re.match(r"^.*Planner finished with a plan of length.*$", line):
            result = float(line[line.find("[")+1:line.find("]")])
    #print(result)
    return result
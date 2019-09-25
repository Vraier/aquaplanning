import os
import re
import csv
import string
import analyseUtil as util
import constants as const

# Strings
csvOutputFolder = "csvExtract"
testFolder = "ttFullSequentialTest"
numTester = 1

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
    return groupedTime

results = getCompTimes(testFolder, numTester)
outFile = os.path.join(csvOutputFolder, "CSV" + testFolder + ".csv")
with open(outFile, 'w') as writeFile:
        writer = csv.writer(writeFile)
        header = ["testNumber"] + list(string.ascii_lowercase)[:len(results)]
        writer.writerow(header)

        for x in range(len(results[0])):
            currRow = [str(x+1)]
            for y in range(len(results)):
                currRow.append(str(results[y][x]))
            writer.writerow(currRow)


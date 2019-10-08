import os
import re
import csv
import string
import analyseUtil as util
import constants as const

# Strings
csvOutputFolder = "csvExtract"
testFolder = "ttHeuAndGB"
numTester = 4

# constant Strings
sequentialFolder = "ttSequentialOptTest"

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

def writeToCSV(timeData):
    outFile = os.path.join(csvOutputFolder, "CSV" + testFolder + ".csv")
    with open(outFile, 'w') as writeFile:
            writer = csv.writer(writeFile)
            header = ["testNumber"] + list(string.ascii_lowercase)[:len(timeData)]
            writer.writerow(header)

            for x in range(len(timeData[0])):
                currRow = [str(x+1)]
                for y in range(len(timeData)):
                    currRow.append(str(timeData[y][x]))
                writer.writerow(currRow)

def mapWithSequential():
    homeDirName = os.path.dirname(__file__)
    parallelFolderPath =  os.path.join(homeDirName, testFolder)
    sequentialFolderPath = os.path.join(homeDirName, sequentialFolder)

    groupedTime = []
    for _ in range(2*numTester + 1):
        groupedTime.append([])

    for folder in os.listdir(sequentialFolderPath):
        currPath = os.path.join(sequentialFolderPath, folder)
        currParallelPath = os.path.join(parallelFolderPath, folder)
        for fileName in os.listdir(currPath):
            filePath = os.path.join(currPath, fileName)
            parallelFilePath = os.path.join(currParallelPath, fileName)

            with open(filePath, 'r') as sequentialFile:
                blocks = util.splitResults(sequentialFile)
                if(not util.foundValidPlan(blocks[0])):
                    for x in range(2*numTester + 1):
                        groupedTime[x].append(const.timeLimit * 10)
                else:
                    with open(parallelFilePath, 'r') as parallelFile:
                        parallelBlocks = util.splitResults(parallelFile)
                        groupedTime[0].append(util.getCalcTime(blocks[0]))
                        for x in range(numTester):
                            if not util.foundValidPlan(parallelBlocks[x]):
                                print("Yay")
                                groupedTime[2*x+1].append(const.timeLimit * 10)
                                groupedTime[2*x+2].append(const.timeLimit * 10)
                            elif util.foundPlanWhileCubing(parallelBlocks[x]):
                                print("Heureca")
                                groupedTime[2*x+1].append(const.timeLimit * 10)
                                groupedTime[2*x+2].append(util.getCalcTime(parallelBlocks[x]))
                            else:
                                groupedTime[2*x+1].append(util.getCalcTime(parallelBlocks[x]))
                                groupedTime[2*x+2].append(const.timeLimit * 10)

    return groupedTime


writeToCSV(mapWithSequential())
import os
import shutil
import errno
import analyseUtil as util
import constants as consts 

# Strings
testResultFolder = "ttFullSequentialTest"
originalDomains = "fullOptAndSatTest"
filteredDomains = "filteredOptAndSat"


homeDirName = consts.homeDirName

for folder in os.listdir(os.path.join(homeDirName, testResultFolder)):
    currPath = os.path.join(homeDirName, testResultFolder, folder)
    testFilePath = os.path.join(homeDirName, originalDomains, folder)
    filteredPath = os.path.join(homeDirName, filteredDomains, folder)
    if not os.path.exists(filteredPath):
        os.mkdir(filteredPath)
        print("Directory " , filteredPath ,  " Created ")
    else:    
        print("Directory " , filteredPath ,  " already exists")
    shutil.copy2(os.path.join(testFilePath, 'domain.pddl'), os.path.join(filteredPath, 'domain.pddl'))

    for fileName in os.listdir(currPath):
        filePath = os.path.join(currPath, fileName)
        testFileData = os.path.join(testFilePath, fileName[:-4])
        filteredData = os.path.join(filteredPath, fileName[:-4])
        if fileName.startswith('p'):
            with open(filePath, 'r') as analyseFile:
                lines = analyseFile.readlines()
                if util.foundValidPlan(lines): #and util.getCalcTime(lines) > 5:
                    shutil.copy2(testFileData, filteredData)
                    #print(os.path.join(homeDirName, 'filtered', folder, fileName))

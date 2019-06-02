import subprocess
import os

dirName = os.path.dirname(__file__)
relativeJarPath = '../target/aquaplanning-0.0.1-SNAPSHOT-jar-with-dependencies.jar'
relativeDomain = '../benchmarks/Childsnack/domain.pddl'
relativeTestCase = '../benchmarks/Childsnack/p09.pddl'

jarPath = os.path.join(dirName, relativeJarPath)
domainPath = os.path.join(dirName, relativeDomain)
testCasePath = os.path.join(dirName, relativeTestCase)

print('Dirname is ' + dirName)
print('jarPath is ' + jarPath)

subprocess.call(['java', '-jar', jarPath, domainPath, testCasePath,])# "-p=cubePlanner", "-T=48", "-v=2", "-c=10000", "--cubeFinder=forwardSearch", "-sched=roundRobin", "-cfs=breadthFirst"])
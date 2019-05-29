import os

domainFile = 'domain.pddl'

def listCombinations(lists):
    if not lists:
        return [[]]
    else:
        result = []
        head, tail = lists[0], lists[1:]
        suffixes = listCombinations(tail)
        for item in head:
            for suffix in suffixes:
                result.append([item] + suffix)
        return result

def hasDomain(path):
    for filename in os.listdir(path):
        if (filename == domainFile):
            return True
    return False
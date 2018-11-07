#!/usr/bin/env python

import json
import subprocess

def runMatch(playerA, playerB):
    return json.loads(subprocess.run(['halite', '--results-as-json', playerA, playerB], stdout=subprocess.PIPE).stdout.decode('utf-8'))

print("test")

#!/usr/bin/env python

import json
import subprocess

def runMatch(playerA, playerB):
    return json.loads(subprocess.run(['halite', '--results-as-json', playerA, playerB], stdout=subprocess.PIPE).stdout.decode('utf-8'))

from trueskill import Rating, quality_1vs1, rate_1vs1

a = trueskill.Rating(mu=25.000, sigma=8.333)
b = trueskill.Rating(mu=25.000, sigma=8.333)

a, b = rate_1vs1(a, b)

# Worker Tasks
# git pull, lock, compile, unlock
# run, lock, get results, update mu/sigma, unlock
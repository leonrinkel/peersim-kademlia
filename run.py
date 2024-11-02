import json
import signal
import pathlib
import argparse
import subprocess

config = """
NODES 11670

TIMEOUT 10000

TRAFFIC_STEP  (SIM_TIME)/NODES
OBSERVER_STEP 100000
TURBULENCE_STEP  (SIM_TIME*20)/NODES

# ::::: network :::::
random.seed SEED

simulation.experiments 1
simulation.endtime SIM_TIME

network.size NODES

# ::::: LAYERS :::::
protocol.0link peersim.jgrapht.GraphTopology
protocol.0link.file caida.graphml

protocol.1grapht peersim.jgrapht.GraphTransport
protocol.1grapht.topo 0link

protocol.2unreltr peersim.transport.UnreliableTransport
protocol.2unreltr.drop 0
protocol.2unreltr.transport 1grapht

protocol.3kademlia peersim.kademlia.KademliaProtocol
protocol.3kademlia.transport 2unreltr
protocol.3kademlia.timeout TIMEOUT
protocol.3kademlia.BITS BITS
protocol.3kademlia.K K
protocol.3kademlia.ALPHA ALPHA
protocol.3kademlia.SORT SORTING

# ::::: INITIALIZERS :::::
init.1uniqueNodeID peersim.kademlia.CustomDistribution
init.1uniqueNodeID.protocol 3kademlia

init.2statebuilder peersim.kademlia.StateBuilder
init.2statebuilder.protocol 3kademlia
init.2statebuilder.transport 2unreltr

# ::::: CONTROLS :::::

# traffic generator
control.0traffic peersim.kademlia.TrafficGenerator
control.0traffic.protocol 3kademlia
control.0traffic.step TRAFFIC_STEP
control.0traffic.topo 0link
control.0traffic.pdist.0 PDIST0
control.0traffic.pdist.1 PDIST1
control.0traffic.pdist.2 PDIST2
control.0traffic.pdist.3 PDIST3
control.0traffic.pdist.4 PDIST4

# ::::: OBSERVER :::::
control.3 peersim.kademlia.KademliaObserver
control.3.protocol 3kademlia
control.3.step OBSERVER_STEP
"""

parser = argparse.ArgumentParser(__package__)
parser.add_argument('--output', type=pathlib.Path, required=True)
parser.add_argument('--seed', type=int, required=True)
parser.add_argument('--sim-time', type=int, default=7200000)
parser.add_argument('--timeout', type=int, default=300)
parser.add_argument('--bits', type=int, default=256)
parser.add_argument('--k', type=int, default=20)
parser.add_argument('--alpha', type=int, default=3)
parser.add_argument('--sorting', type=int, default=0)
parser.add_argument('--pdist0', type=float, default=0.7)
parser.add_argument('--pdist1', type=float, default=0.3)
parser.add_argument('--pdist2', type=float, default=0.0)
parser.add_argument('--pdist3', type=float, default=0.0)
parser.add_argument('--pdist4', type=float, default=0.0)
args = parser.parse_args()

with open('example.cfg', mode='w', encoding='utf-8') as conf:
    conf.write(
        f'SEED {args.seed}\n'
        f'SIM_TIME {args.sim_time}\n'

        f'BITS {args.bits}\n'
        f'K {args.k}\n'
        f'ALPHA {args.alpha}\n'
        f'SORTING {args.sorting}\n'

        f'PDIST0 {args.pdist0}\n'
        f'PDIST1 {args.pdist1}\n'
        f'PDIST2 {args.pdist2}\n'
        f'PDIST3 {args.pdist3}\n'
        f'PDIST4 {args.pdist4}\n'
    )
    conf.write(config)

def timeout_handler(sig, frame):
    raise Exception('timeout')

signal.signal(signal.SIGALRM, timeout_handler)
signal.alarm(args.timeout)

with open(args.output, mode='w', encoding='utf-8') as log:
    try:
        conf = vars(args)
        conf.pop('output')
        log.write(f'config {json.dumps(conf)}')

        subprocess.call(
            [
                'java',
                '-cp', 'app.jar',
                'peersim.Simulator',
                'example.cfg',
            ],
            stderr=log,
        )
    except Exception as e:
        log.write(f'exception {e}')

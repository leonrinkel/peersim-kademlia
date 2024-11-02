#!/bin/bash

function run_sim {
    # try different k
    for K in 10 20 30
    do
        # simulate a couple of times
        for i in `seq 1 10`
        do
            SEED=$RANDOM

            # with/without sorting
            for SORTING in `seq 0 2`
            do
                file="runs/run-$RANDOM.log"
                python3 run.py \
                    --output $file \
                    --seed $SEED \
                    --k $K \
                    --sorting $SORTING \
                    --sim-time 720000000 \
                    --timeout 600 \
                    --pdist0 $PDIST0 \
                    --pdist1 $PDIST1 \
                    --pdist2 $PDIST2 \
                    --pdist3 $PDIST3 \
                    --pdist4 $PDIST4
            done
        done
    done
}

# intra-as
PDIST0=1.000
PDIST1=0.000
PDIST2=0.000
PDIST3=0.000
PDIST4=0.000
run_sim

# very close
PDIST0=0.700
PDIST1=0.300
PDIST2=0.000
PDIST3=0.000
PDIST4=0.000
run_sim

# close
PDIST0=0.400
PDIST1=0.300
PDIST2=0.200
PDIST3=0.075
PDIST4=0.025
run_sim

# even
PDIST0=0.200
PDIST1=0.200
PDIST2=0.200
PDIST3=0.200
PDIST4=0.200
run_sim

# mean 2
PDIST0=0.100
PDIST1=0.200
PDIST2=0.400
PDIST3=0.200
PDIST4=0.100
run_sim

# far
PDIST0=0.025
PDIST1=0.075
PDIST2=0.200
PDIST3=0.300
PDIST4=0.400
run_sim

# very far
PDIST0=0.000
PDIST1=0.000
PDIST2=0.000
PDIST3=0.300
PDIST4=0.700
run_sim

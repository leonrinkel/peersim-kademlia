#!/bin/bash

bits_options=(32 128 256)
k_options=(10 20 40)

function run_sim {
    for BITS in ${bits_options[@]}; do
        for K in ${k_options[@]}; do
            # simulate a couple of times
            for i in `seq 1 10`
            do
                SEED=$RANDOM

                # with/without sorting
                for SORTING in `seq 0 1`
                do
                    # create config
                    echo "" > example.cfg
                    echo "PDIST0 $PDIST0" >> example.cfg
                    echo "PDIST1 $PDIST1" >> example.cfg
                    echo "PDIST2 $PDIST2" >> example.cfg
                    echo "PDIST3 $PDIST3" >> example.cfg
                    echo "PDIST4 $PDIST4" >> example.cfg
                    echo "SEED $SEED" >> example.cfg
                    echo "SORTING $SORTING" >> example.cfg
                    echo "BITS $BITS" >> example.cfg
                    echo "K $K" >> example.cfg
                    cat baseline.cfg >> example.cfg

                    # write log
                    file="run-$RANDOM.log"
                    echo "" > $file
                    echo "PDIST0 $PDIST0" >> $file
                    echo "PDIST1 $PDIST1" >> $file
                    echo "PDIST2 $PDIST2" >> $file
                    echo "PDIST3 $PDIST3" >> $file
                    echo "PDIST4 $PDIST4" >> $file
                    echo "SEED $SEED" >> $file
                    echo "SORTING $SORTING" >> $file
                    echo "BITS $BITS" >> $file
                    echo "K $K" >> $file
                    echo "" >> $file

                    ./gradlew :app:run --args="example.cfg" >> $file 2>&1
                done
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

# kinda close
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

# kinda far
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
PDIST3=0.700
PDIST4=0.300
run_sim

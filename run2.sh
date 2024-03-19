#!/bin/bash

options=(
    # baseline
    '256,20,3,5000'
    # vary bits
    '512,20,3,5000'
    '128,20,3,5000'
    '12,20,3,5000'
    '8,20,3,5000'
    # vary k
    '256,100,3,5000'
    '256,40,3,5000'
    '256,10,3,5000'
    '256,1,3,5000'
    # vary alpha
    '256,20,1,5000'
    '256,20,2,5000'
    '256,20,10,5000'
    '256,20,100,5000'
    # vary nodes
    '256,20,3,1000'
    '256,20,3,2000'
    '256,20,3,6000'
    '256,20,3,7000'
)

# chosen by fair dice roll
seeds=(
    8646
    31356
    10589
    26552
    1877
    14328
    26327
    17539
    24481
    11486
)

function run_sim {
    # try different options
    for row in "${options[@]}"
    do
        while IFS=',' read -r bits k alpha nodes
        do

            # with/without sorting
            for sorting in `seq 0 1`
            do

                # simulate couple of times
                for seed in "${seeds[@]}"
                do

                    # create config
                    echo "" > example.cfg
                    echo "PDIST0 $pdist0" >> example.cfg
                    echo "PDIST1 $pdist1" >> example.cfg
                    echo "PDIST2 $pdist2" >> example.cfg
                    echo "PDIST3 $pdist3" >> example.cfg
                    echo "PDIST4 $pdist4" >> example.cfg
                    echo "BITS $bits" >> example.cfg
                    echo "K $k" >> example.cfg
                    echo "ALPHA $alpha" >> example.cfg
                    echo "NODES $nodes" >> example.cfg
                    echo "SORTING $sorting" >> example.cfg
                    echo "SEED $seed" >> example.cfg
                    cat baseline.cfg >> example.cfg

                    # write log
                    file="run-$RANDOM.log"
                    echo "" > $file
                    echo "PDIST0 $pdist0" >> $file
                    echo "PDIST1 $pdist1" >> $file
                    echo "PDIST2 $pdist2" >> $file
                    echo "PDIST3 $pdist3" >> $file
                    echo "PDIST4 $pdist4" >> $file
                    echo "BITS $bits" >> $file
                    echo "K $k" >> $file
                    echo "ALPHA $alpha" >> $file
                    echo "NODES $nodes" >> $file
                    echo "SORTING $sorting" >> $file
                    echo "SEED $seed" >> $file
                    echo "" >> $file

                    ./gradlew :app:run --args="example.cfg" >> $file 2>&1
                done
            done
        done <<< "$row"
    done
}

# intra-as
pdist0=1.000
pdist1=0.000
pdist2=0.000
pdist3=0.000
pdist4=0.000
run_sim

# very close
pdist0=0.700
pdist1=0.300
pdist2=0.000
pdist3=0.000
pdist4=0.000
run_sim

# kinda close
pdist0=0.400
pdist1=0.300
pdist2=0.200
pdist3=0.075
pdist4=0.025
run_sim

# even
pdist0=0.200
pdist1=0.200
pdist2=0.200
pdist3=0.200
pdist4=0.200
run_sim

# mean 2
pdist0=0.100
pdist1=0.200
pdist2=0.400
pdist3=0.200
pdist4=0.100
run_sim

# kinda far
pdist0=0.025
pdist1=0.075
pdist2=0.200
pdist3=0.300
pdist4=0.400
run_sim

# very far
pdist0=0.000
pdist1=0.000
pdist2=0.000
pdist3=0.700
pdist4=0.300
run_sim

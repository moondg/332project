#!/bin/sh

# simple shell script that clear cache files and run worker

# Check if exactly one argument is provided
if [ $# -ne 2 ]; then
    echo "Error: Exactly two arguments are required."
    echo "1: small/big/large"
    echo "2: master's IP:Port"
    exit 1
fi

if [[ $1 =~ small$ || $1 =~ big$ || $1 =~ large$ ]]; then
    rm ~/output/$1/*
    sbt "run $2 -I /home/dataset/$1 -O /home/red/output/$1"
    3 # specific argument for our project
else
    echo "Available options: small/big/large"
    exit 1
fi

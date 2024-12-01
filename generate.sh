#!/bin/sh

# generate data with gensort
# use this with `bash generate.sh #OF_File FOLDER_NAME`
# execute this in `data` folder

# Check if exactly three argument is provided
if [ $# -ne 2 ]; then
    echo "Error: Exactly three argument is required."
    echo "generate NUMBER_OF_FILE FOLDER"
    echo "ex) bash generate.sh 10 big"
    echo "Execute this in data/ folder!!"
    exit 1
else
    mkdir $2
    for((i=0;i<$1;i++)); do
        s=$((i*320000))
        ../64/gensort -a -b$s 320000 $2/partition$i
    done
    exit 1
fi
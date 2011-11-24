#!/bin/bash

DIR=.
FILE=nodes
NBR_NODES=""
NODES=""

usage () {
cat <<EOF

usage: $0 [-d path] [-f file]

     Starts /path/pcounter on each node specified on /path. Any output from pcounter 
     is redirected to /path/pcounter-hostname.out

     -d path
        Specifies the directory path for the program pcounter. 
        The output file (any print statements) for pcounter is also stored here.
        Default path: "."

     -f file
        List of remote hosts (ip address or hostname) where pcounter is to be started. 
        The file must contain only one host per line.
        Default file: "nodes"
EOF
}

# read command line options
while getopts "d:f:" opt 
do
    case $opt in
        d)
            DIR=$OPTARG
            echo -e "Directory: $DIR"
            ;;
        f)
            FILE=$OPTARG
            echo -e "Filename: $FILE"
            ;;
        \?)
            echo "Invalid option -$OPTARG" >&2
            usage
            exit 1
            ;;
        :)
            echo "Option -$OPTARG requires an argument." >&2
            usage
            exit 1
            ;;
     esac
done

# read nodes from file
FILE=$DIR/$FILE
old_IFS=$IFS
IFS=$'\n'
node=($(cat $FILE))
IFS=$old_IFS

NBR_NODES=${#node[*]}
NODES=${node[@]}

echo -e "\nStarting pcounter on $NBR_NODES nodes:"
echo -e "\tNode\t\t\tOutput file"
echo -e "\t----\t\t\t-----------"
k=0
for i in $NODES; do 
    let "k += 1"
    OUT_FILE=pcounter-$i.out
    echo -e "\t$i\t\t\t$DIR/$OUT_FILE"; 
	ssh -q $i "nohup ./pcounter &> $OUT_FILE < /dev/null &"; 
    if [ $k == $NBR_NODES ] ; then
        ssh -q $i "nohup ./pcounter init &> $OUT_FILE < /dev/null &"; 
	fi
done

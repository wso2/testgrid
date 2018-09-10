#!/usr/bin/env bash

echo "dummy infrastructure provisioning for tests"

while [ "$1" != "" ]; do
    PARAM=`echo $1 | awk -F= '{print $1}'`
    VALUE=`echo $1 | awk -F= '{print $2}'`
    case $PARAM in
        -h | --help)
            usage
            exit
        ;;
        --workspace | -w | --output-dir | -o)
            WORKSPACE=${VALUE}
            echo Workspace: ${WORKSPACE}
        ;;
        *)
            echo "ERROR: unknown parameter \"$PARAM\""
            usage
            exit 1
        ;;
    esac
    shift
done

echo 'PWD:' `pwd`
echo 'We are in:' `uname`

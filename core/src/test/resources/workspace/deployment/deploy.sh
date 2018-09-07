#!/usr/bin/env bash

echo "dummy deployment creation for tests"

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

INFRA=$WORKSPACE/workspace/infrastructure
echo $(pwd)
if [ ! -d $INFRA ]; then
  echo 'Cannot proceed the deployment. infrastructure dir is empty: ' ${INFRA}
  exit 1;
fi

echo "Deploy server1" > $INFRA/my-deployment.txt
cat $INFRA/my-deployment.txt

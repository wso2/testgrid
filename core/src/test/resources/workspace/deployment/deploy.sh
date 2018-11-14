#!/usr/bin/env bash

echo "dummy deployment creation for tests"

DIR=$2
echo $DIR
FILE=${DIR}/infrastructure.properties

cat $FILE
PROP_KEY=DEPLOY_VALUE

#----------------------------------------------------------------------
# getting data from databuckets
#----------------------------------------------------------------------
value=`grep -w "$PROP_KEY" ${FILE} | cut -d'=' -f2`
echo "value is $value"
if [ "$value" != "deploy_value1" ];then
    exit 1;
fi

echo "deployment succesful"

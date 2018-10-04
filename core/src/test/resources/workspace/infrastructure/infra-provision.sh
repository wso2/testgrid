#!/usr/bin/env bash

DIR=$2
echo $DIR
FILE=${DIR}/testplan-props.properties

PROP_KEY=PROPERTY1

#----------------------------------------------------------------------
# getting data from databuckets
#----------------------------------------------------------------------
value=`grep -w "$PROP_KEY" ${FILE} | cut -d'=' -f2`
echo "value is $value"
if [ "$value" != "value1" ];then
    exit 1;
fi

echo -e "\nDEPLOY_VALUE=deploy_value1" >> ${DIR}/testplan-props.properties

echo "infra provision succesful"
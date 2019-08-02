#!/bin/bash

set -o xtrace
echo "Running dummy infra.sh"
echo "I got these parameters from testplan-props.properties:"

OUTPUT_DIR=$4
cat $OUTPUT_DIR/testplan-props.properties

#grep -i ADOPT $OUTPUT_DIR/testplan-props.properties && exit 124

#exit 0;

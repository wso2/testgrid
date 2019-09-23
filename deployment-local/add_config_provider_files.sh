#!/bin/sh

host=http://localhost:8080/admin

while [ $(curl -s -w "%{http_code}" $host/cli -o /dev/null) -eq 503 ]
do
 sleep 5
done

curl -X POST $host/configuration-as-code/replace?_.newSource=configuration_as_code.yaml&json={"newSource":"configuration.yaml"}&replace=Apply%20new%20configuration

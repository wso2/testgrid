#!/bin/sh

host=http://localhost:8080/admin
curl -X POST $host/configuration-as-code/replace?_.newSource=configuration_as_code.yaml&json={"newSource":"configuration_as_code.yaml"}&replace=Apply%20new%20configuration

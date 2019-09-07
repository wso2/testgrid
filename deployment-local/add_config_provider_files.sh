#!/bin/bash

host=http://localhost:8080/admin
curl -X POST $host/configuration-as-code/replace?_.newSource=CasC.yaml&json={"newSource":"CasC.yaml"}&replace=Apply%20new%20configuration



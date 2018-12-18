#!/bin/bash

while true; do
    if [ $(systemctl is-active testgrid-agent) == "active" ]; then
        break
    fi
    sudo systemctl start testgrid-agent
    sleep 1
done
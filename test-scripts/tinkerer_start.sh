#!/bin/bash
which java > /opt/testgrid/agent/thr.log
sudo systemctl start --no-block testgrid-agent >> /opt/testgrid/agent/thr.log


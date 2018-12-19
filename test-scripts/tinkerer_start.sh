#!/bin/bash
which java > /opt/testgrid/agent/thr.log
sudo systemctl start testgrid-agent

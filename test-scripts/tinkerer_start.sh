#!/bin/bash
which java > /opt/testgrid/agent/thr.log
systemctl daemon-reload
sudo systemctl start testgrid-agent
systemctl start testgrid-agent

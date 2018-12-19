#!/bin/bash
which java > /opt/testgrid/agent/thr.log
systemctl daemon-reload >> /opt/testgrid/thr/log
sudo systemctl start testgrid-agent >> /opt/testgrid/thr/log
systemctl start testgrid-agent >> /opt/testgrid/thr/log

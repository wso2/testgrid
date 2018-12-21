#!/bin/bash

systemctl daemon-reload >> /opt/testgrid/thr/log
sudo systemctl start testgrid-agent >> /opt/testgrid/thr/log


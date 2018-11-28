#!/bin/bash

DIR=$2

echo 'Downloading dummy surefire-reports'
wget https://github.com/tharindu1992/Test-Phase1/raw/master/Dummy1.zip

ls

echo 'Unzip downloaded files into data bucket'
unzip Dummy1.zip -d $DIR

echo 'Files in data bucket:'
ls $DIR

echo 'test.sh complete'
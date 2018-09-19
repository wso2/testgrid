# Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#!/bin/sh

pem_file=/opt/wso2/sshkeys/ssh_key

#delete the lock on exit
trap 'rm -rf /var/lock/depsync-lock' EXIT

mkdir /tmp/carbon-rsync-logs/


#keep a lock to stop parallel runs
if mkdir /var/lock/depsync-lock; then
  echo "Locking succeeded" >&2
else
  echo "Lock failed - exit" >&2
  exit 1
fi


#get the workers-list.txt
pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd`
popd > /dev/null
echo $SCRIPTPATH

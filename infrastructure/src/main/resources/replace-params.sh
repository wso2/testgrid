# ----------------------------------------------------------------------------
#
# Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# ----------------------------------------------------------------------------

#!/bin/bash

strindex() {
  x="${1%%$2*}"
  [[ "$x" = "$1" ]] && echo -1 || echo "${#x}"
}

PARAM_PP_FILE=""

for param in "$@"
do
    idx=$(strindex $param =)
    paramName=${param:0:$idx}
    if [[ $paramName == "params-file" ]]; then
          PARAM_PP_FILE=${param:$idx+1}
          break
    fi
done

for param in "$@"
do
    idx=$(strindex $param =)
    paramName=${param:0:$idx}
    paramValue=${param:$idx+1}
    sed -i "s|${paramName}\s=\s.*|${paramName} = '${paramValue}'|g" "$PARAM_PP_FILE"
done
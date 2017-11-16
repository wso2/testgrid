#!/bin/bash
# ------------------------------------------------------------------------
#
# Copyright 2016 WSO2, Inc. (http://wso2.com)
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
# limitations under the License

# ------------------------------------------------------------------------
prgdir=$(dirname "$0")
script_path=$(cd "$prgdir"; pwd)
common_folder=$(cd "${script_path}/../common/scripts/"; pwd)

product_profiles=(default)

full_deployment=false

while getopts :f FLAG; do
    case $FLAG in
        f)
            full_deployment=true
            ;;
    esac
done

if [[ ! -z $product_profiles ]]; then
    for profile in ${product_profiles[@]}; do
        bash "${common_folder}/undeploy.sh" "$profile"
    done
else
    bash "${common_folder}/undeploy.sh"
fi

sleep 5

if [ $full_deployment == true ]; then
    echo "Undeploying MySQL Services and RCs for Conf and Gov remote mounting..."
    bash $script_path/../common/wso2-shared-dbs/undeploy.sh
fi

# undeploy DB service, rc and pods
kubectl delete rc,services,pods -l name="mysql-is-db"

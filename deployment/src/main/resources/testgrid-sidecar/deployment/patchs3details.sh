# Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
#

# Deployment namespace
S3_KEY_ID=$1
S3_SECRET_KEY=$2
S3_REGION=$3
S3_BUCKET=$4

sed -e "s|\${S3_KEY_ID}|${S3_KEY_ID}|g" | sed -e "s|\${S3_SECRET_KEY}|${S3_SECRET_KEY}|g" | sed -e "s|\${S3_REGION}|${S3_REGION}|g" | sed -e "s|\${S3_BUCKET}|${S3_BUCKET}|g"

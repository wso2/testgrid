# ---------------------------------------------------------------------------
#        Copyright 2018 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

echo "Running Powershell command"
"wsEndpoint=$($args[0])"| Add-Content "C:\testgrid\app\agent-config.properties"
"region=$($args[1])"| Add-Content "C:\testgrid\app\agent-config.properties"
"testPlanId=$($args[2])"| Add-Content "C:\testgrid\app\agent-config.properties"
"provider=$($args[3])"| Add-Content "C:\testgrid\app\agent-config.properties"
"userName=$($args[4])"| Add-Content "C:\testgrid\app\agent-config.properties"
"password=$($args[5])"| Add-Content "C:\testgrid\app\agent-config.properties"
"instanceId=$(Invoke-WebRequest 'http://169.254.169.254/latest/meta-data/instance-id')" | Add-Content "C:\testgrid\app\agent-config.properties"
"instanceIP=$(Invoke-WebRequest 'http://169.254.169.254/latest/meta-data/public-ipv4')" | Add-Content "C:\testgrid\app\agent-config.properties"

$agentPIDPath = "C:/testgrid/app/agent/testgrid-agent.pid"

# Start running agent
function startAgent {
    $agentProcess = start-process -FilePath java -ArgumentList '-classpath C:\testgrid\app\agent\agent.jar;C:\testgrid\app\agent\libs\* org.wso2.testgrid.agent.AgentApplication' -PassThru
    $processId = $agentProcess.Id
    write-host "service added to pid $processId"
    $agentProcess.Id | out-file -filepath $agentPIDPath
}

# Stop running agent
function stopAgent {
	if (test-path $agentPIDPath) {
		$processId = get-content -Path $agentPIDPath
    	write-host "Read pid is $processId"
    	stop-process -Id $processId
	} else {
		write-host "PID file not found"
	} 
}

# Restart agent
function restartAgent {
    stopAgent
    startAgent
}
startAgent
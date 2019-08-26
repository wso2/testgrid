function executearchive(){
    reducedfilepath=$(reducefilepath $3 $podname $4)
    createreadme $1 $3 $4
    zip -r -j "$2.zip" $1
    mv --backup=t "$2.zip" "/opt/testgrid/commonarchives/$reducedfilepath.zip"
}

function transfer(){
    zip -r -j logfiles.zip /opt/testgrid/commonarchives/
    mv logfiles.zip /var/log/product_dumps.zip
}

function startup(){
   mkdir /opt/testgrid/commonarchives
}

function reducefilepath(){
  filepath=${1::8}_${1: -5}:${2::8}_${2: -5}:${3::8}_${3: -5}
  echo $filepath
}

function createreadme(){
   cat > "$1/info.txt" << EOF
DEPLOYMENT_NAME=${3}
POD_NAME=${podname}
CONTAINER_NAME=${2}
NODE_NAME=${nodename}
LOCAL_IP=${podip}
TESTPLAN_ID=${testPlanId}
NAMESPACE=${podnamespace}
EOF
}

startup

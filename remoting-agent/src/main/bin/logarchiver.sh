function executearchive(){
    zip -r "$2.zip" $1
}

function transfer(){
    cd /opt/testgrid/commonarchives || exit
    echo "transferring data"
}

function startup(){
   mkdir /opt/testgrid/commonarchives
}

startup

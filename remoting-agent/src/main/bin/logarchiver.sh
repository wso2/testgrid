function executearchive(){
    cd $1 || exit
    zip -r $2
    cp "$2.zip" /opt/testgrid/commonarchives
}

function transfer(){
    cd /opt/testgrid/commonarchives || exit
    echo "transferring data"
}

function startup(){
   mkdir /opt/testgrid/commonarchives
}

startup

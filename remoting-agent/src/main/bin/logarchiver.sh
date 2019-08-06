function executearchive(){
    zip -r "$2.zip" $1
    mv "$2.zip" "/opt/testgrid/commonarchives/$2.zip"
}

function transfer(){
    zip -r /opt/testgrid/commonarchives/ logfiles.zip
    # Transport logic here
}

function startup(){
   mkdir /opt/testgrid/commonarchives
}

startup

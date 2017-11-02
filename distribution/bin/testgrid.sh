#!/bin/sh


echo "__          _______  ____ ___    _______        _    _____      _     _ "
echo "\ \        / / ____|/ __ \__ \  |__   __|      | |  / ____|    (_)   | |"
echo " \ \  /\  / / (___ | |  | | ) |    | | ___  ___| |_| |  __ _ __ _  __| |"
echo "  \ \/  \/ / \___ \| |  | |/ /     | |/ _ \/ __| __| | |_ |  __| |/ _  |"
echo "   \  /\  /  ____) | |__| / /_     | |  __/\__ \ |_| |__| | |  | | (_| |"
echo "    \/  \/  |_____/ \____/____|    |_|\___||___/\__|\_____|_|  |_|\____|"
echo ""                                                                         
                                                                         

echo "/********************Welcome to WSO2 TestGrid:0.9***************************/"
echo "Please choose the product from the List"
echo ""
echo "1.WSO2 IS"
echo "2.WSO2 EI"
echo "3.WSO2 APIM"
echo "4.WSO2 AS"
echo ""


valid=0
while [ "$valid" -eq "0" ]
do
echo -n "Please Enter the index of product :"
read product
    for  number in 1 2 3 4
    do
    if [ "$number" -eq "$product" ]
    then 
        valid=1
        break
    else
        valid=0
    fi
    done
if [ "$valid" -eq "0" ] 
	then echo "Invalid index !!"
fi
done

case $product in
	1) 
	product="WSO2 Identity Server"
	echo -n "Please Enter the WSO2 IS version :"
	read version
	resource_repo="https://github.com/sameerawickramasekara/test-grid-is-resources.git" 
	;;
	2)
	product="WSO2 Identity Server"
	echo -n "Please Enter the WSO2 EI version :"
	read version	
	resource_repo="https://github.com/sameerawickramasekara/test-grid-is-resources.git" 	
	;;
	3)
	product="WSO2 Identity Server"
	echo -n "Please Enter the WSO2 APIM version :"
	read version 
	resource_repo="https://github.com/sameerawickramasekara/test-grid-is-resources.git" 
	;;
	4)
	product="WSO2 Identity Server"
	echo -n "Please Enter the WSO2 AS version :"
	read version
	resource_repo="https://github.com/sameerawickramasekara/test-grid-is-resources.git" 
	;;
esac
echo "Starting execution......"

java -jar test-grid-0.9.0-SNAPSHOT.jar "$resource_repo" "$product" "$version"



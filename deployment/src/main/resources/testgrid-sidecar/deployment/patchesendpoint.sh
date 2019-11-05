ESEP=$1
if  [[ $ESEP == https://* ]]  ;
then :
  oldString=https://
  repString=http://
  ESEP=$(echo ${ESEP/$oldString/$repString})
elif [[ $ESEP == http:// ]] ;
then  :
else
   ESEP=http://$1
fi
sed -e "s|\${ESEP}|${ESEP}|g"

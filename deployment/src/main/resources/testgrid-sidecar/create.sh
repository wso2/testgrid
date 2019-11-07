
namespace=$1
esEP=$2
req=$3
filename=$4


chmod 777 ./testgrid-sidecar/create.sh
chmod 777 ./testgrid-sidecar/deployment/patchnamespace.sh
chmod 777 ./testgrid-sidecar/deployment/webhook-create-signed-cert.sh
chmod 777 ./testgrid-sidecar/deployment/webhook-patch-ca-bundle.sh
chmod 777 ./testgrid-sidecar/deployment/patchesendpoint.sh

kubectl create configmap --dry-run logpath-config --from-file=./testgrid-sidecar/deployment/logstash-details.yaml --output yaml | tee ./testgrid-sidecar/deployment/logpath-configmap.yaml

if [[ "$req" == "SidecarReq" ]]
then
  logstashconffile="./testgrid-sidecar/deployment/confs/logstash.conf"
  if [ -f $logstashconffile ] ; then
      rm $logstashconffile
  fi
  cp ./testgrid-sidecar/deployment/confs/${filename} ${logstashconffile}
  kubectl create configmap --dry-run logstash-conf --from-file=./testgrid-sidecar/deployment/confs/logstash.conf --output yaml | tee ./testgrid-sidecar/deployment/logconf.yaml
fi

./testgrid-sidecar/deployment/webhook-create-signed-cert.sh \
    --service sidecar-injector-webhook-svc \
    --secret sidecar-injector-webhook-certs \
    --namespace ${namespace}

cat ./testgrid-sidecar/deployment/mutatingwebhook.yaml | \
    ./testgrid-sidecar/deployment/webhook-patch-ca-bundle.sh ${namespace}> \
    ./testgrid-sidecar/deployment/mutatingwebhook_temp.yaml

cat ./testgrid-sidecar/deployment/mutatingwebhook_temp.yaml | \
    ./testgrid-sidecar/deployment/patchnamespace.sh ${namespace} > \
    ./testgrid-sidecar/deployment/mutatingwebhook-ca-bundle.yaml

cat ./testgrid-sidecar/deployment/conf_template.yaml | \
    ./testgrid-sidecar/deployment/patchesendpoint.sh ${esEP} > \
    ./testgrid-sidecar/deployment/configmap.yaml

cat ./testgrid-sidecar/deployment/logstash-collector-template.yaml | \
    ./testgrid-sidecar/deployment/patchesendpoint.sh ${esEP} > \
    ./testgrid-sidecar/deployment/logstash-collector.yaml

if [[ "$req" == "SidecarReq" ]]
then
  kubectl create -f ./testgrid-sidecar/deployment/filebeatyaml.yaml --namespace ${namespace}
  kubectl create -f ./testgrid-sidecar/deployment/logstashyaml.yaml --namespace ${namespace}
  kubectl create -f ./testgrid-sidecar/deployment/logconf.yaml --namespace ${namespace}
  kubectl create -f ./testgrid-sidecar/deployment/logstash-collector.yaml --namespace ${namespace}
  kubectl create -f ./testgrid-sidecar/deployment/logstash-service.yaml --namespace ${namespace}
fi

kubectl create -f ./testgrid-sidecar/deployment/logpath-configmap.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/configmap.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/deployment.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/service.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/mutatingwebhook-ca-bundle.yaml --namespace ${namespace}



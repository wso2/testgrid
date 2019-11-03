namespace=$1

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

kubectl create -f ./testgrid-sidecar/deployment/logpath-configmap.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/additional-configmaps.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/configmap.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/deployment.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/service.yaml --namespace ${namespace}
kubectl create -f ./testgrid-sidecar/deployment/mutatingwebhook-ca-bundle.yaml --namespace ${namespace}



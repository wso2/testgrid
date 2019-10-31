./deployment/webhook-create-signed-cert.sh \
    --service sidecar-injector-webhook-svc \
    --secret sidecar-injector-webhook-certs \
    --namespace ${namespace}

cat deployment/mutatingwebhook.yaml | \
    deployment/webhook-patch-ca-bundle.sh ${namespace}> \
    deployment/mutatingwebhook_temp.yaml

cat deployment/mutatingwebhook_temp.yaml | \
    deployment/patchnamespace.sh ${namespace} > \
    deployment/mutatingwebhook-ca-bundle.yaml

kubectl create -f deployment/addtional-configmaps.yaml --namespace ${namespace}
kubectl create -f deployment/configmap.yaml --namespace ${namespace}
kubectl create -f deployment/deployment.yaml --namespace ${namespace}
kubectl create -f deployment/service.yaml --namespace ${namespace}
kubectl create -f "deployment/mutatingwebhook-ca-bundle.yaml" --namespace ${namespace}


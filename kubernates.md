
```Kubernates commands``` 

1. kubectl get nodes  - get the nodes of the kubernates cluster
2. kubectl get pods - get the pods of the kubernates cluster```
3. kubectl describe deployments - describe the deployments
4. kubectl get deployments - get all the deployments

   kubectl exec jdoodle-deploy-6b6cf6459-s862q -- curl -s http://localhost

minikube
https://minikube.sigs.k8s.io/docs/tutorials/multi_node/

Start minikube with 2 nodes
minikube start --nodes 2 -p jdoodle-multinode
minikube status -p jdoodle-multinode

For local image reference while starting a deployment
https://medium.com/swlh/how-to-run-locally-built-docker-images-in-kubernetes-b28fbc32cc1d

minikube 


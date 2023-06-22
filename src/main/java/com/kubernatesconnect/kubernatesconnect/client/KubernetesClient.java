package com.kubernatesconnect.kubernatesconnect.client;

import com.kubernatesconnect.kubernatesconnect.constants.AppConstants;
import io.kubernetes.client.extended.kubectl.Kubectl;
import io.kubernetes.client.extended.kubectl.exception.KubectlException;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static io.kubernetes.client.extended.kubectl.Kubectl.drain;
import static io.kubernetes.client.extended.kubectl.Kubectl.exec;


import java.io.IOException;


@Component
public class KubernetesClient {
    private final static Logger logger = LoggerFactory.getLogger(KubernetesClient.class);
    private ApiClient apiclient  = null;


    public KubernetesClient(){
        try {
            logger.info("Connecting to Kubernetes client at local");
            apiclient = Config.defaultClient();
            Configuration.setDefaultApiClient(apiclient);
            logger.info("Connection initilized successfully");
        } catch (IOException e) {
            logger.error("Got exception while connecting to kumbernestis client :{}", e);
        }
    }

    public V1PodList getPods(){
        CoreV1Api api = new CoreV1Api();
        V1PodList list;
        try {
            list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        for (V1Pod item : list.getItems()) {
            System.out.println(item.getMetadata().getName());
        }

        return list;
    }

    public boolean stopDockerk8s() throws KubectlException {
        logger.info("Inside KunbernetesClient class and stopDockerk8s");
        try {
            Kubectl.delete(V1Deployment.class)
                    .namespace(AppConstants.NAMESPACE)
                    .name(AppConstants.DEPLOYMENTNAME)
                    .execute();
        } catch(Exception ex){
            logger.error("Exception while trying to stop the deployment : {}", ex );
            return false;
        }
        return true;
    }

    public boolean createStartDockerk8s() throws ApiException {
        logger.info("Inside KunbernetesClient class and createStartDockerk8s");
        StartDeploymentKubernates kubernatesStart = new StartDeploymentKubernates(this.apiclient);
        kubernatesStart.startKubernatesDeployment();
        return true;
    }

    public String getPagek8s() {
        logger.info("Inside Kubernetes Client class and getPagek8s()");


        return "Hello for now";
    }
}


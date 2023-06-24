package com.kubernatesconnect.kubernatesconnect.service;

import com.kubernatesconnect.kubernatesconnect.client.KubernetesClient;
import io.kubernetes.client.extended.kubectl.exception.KubectlException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1PodList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KubernatesClientService {
    private final static Logger logger = LoggerFactory.getLogger(KubernetesClient.class);
    public KubernetesClient client = null;

    public KubernatesClientService(KubernetesClient client) {
        this.client = client;
    }

    public V1PodList listPods() {
       return this.client.getPods();
    }

    public boolean createStartDockerk8s() throws ApiException {
        logger.info("Inside KubernatesClientService class :createStartDockerk8s()");
        return this.client.createStartDockerk8s();
    }

    public boolean stopDockerk8s() throws KubectlException {
        logger.info("Inside KubernatesClientService class : stopDockerk8s()");
        return this.client.stopDockerk8s();
    }

    public String getPagek8s(){
        logger.info("Inside KubernatesClientService class : getPagek8s()");
        return this.client.getPagek8s();
    }
}

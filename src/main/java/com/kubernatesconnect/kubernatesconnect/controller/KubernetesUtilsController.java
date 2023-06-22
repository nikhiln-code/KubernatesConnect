package com.kubernatesconnect.kubernatesconnect.controller;

import com.kubernatesconnect.kubernatesconnect.service.KubernatesClientService;
import io.kubernetes.client.extended.kubectl.exception.KubectlException;
import io.kubernetes.client.openapi.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kubeutils")
public class KubernetesUtilsController {
    private static final Logger logger = LoggerFactory.getLogger(KubernetesUtilsController.class);
    @Autowired
    KubernatesClientService service ;

    @GetMapping("/create-start-docker-k8s")
    public Boolean createStartDockerk8s() throws ApiException {
        logger.info("Got the request for createServerDockerk8s");
        return service.createStartDockerk8s();
    }

    @GetMapping("/stop-docker-k8s")
    public Boolean createStopDockerk8s() throws KubectlException {
        logger.info("Got the request for createStopDockerk8s");
        return service.stopDockerk8s();
    }

    @GetMapping("/get-page-k8s")
    public String getpagek8s(){
        logger.info("Got the request for getpagek8s");
        return service.getPagek8s();
    }

}

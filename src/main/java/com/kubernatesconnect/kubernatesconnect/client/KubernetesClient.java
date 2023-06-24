package com.kubernatesconnect.kubernatesconnect.client;

import com.kubernatesconnect.kubernatesconnect.constants.AppConstants;
import io.kubernetes.client.Exec;
import io.kubernetes.client.extended.kubectl.Kubectl;
import io.kubernetes.client.extended.kubectl.exception.KubectlException;
import io.kubernetes.client.extended.wait.Wait;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Streams;
import okhttp3.Call;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;

import java.util.List;
import java.util.Map;


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

    public String getPagek8sV1() {
        logger.info("Inside Kubernetes Client class and getPagek8s()");
        try{
            CoreV1Api api = new CoreV1Api();
            CallBackForOutput callback = new CallBackForOutput();

            Call call = api.connectGetNamespacedPodExecCall(
                    "jdoodle-deploy-6b6cf6459-s862q", "default", "-- curl -s http://localhost",
                    "bcbd84684f9725c2774c491c5aa6c3eb3e38fb946d825d7dabb85655d86ecb05", false, false, true, false, callback);
           call.execute();

            Wait.poll(
                    Duration.ofSeconds(3),
                    Duration.ofSeconds(60),
                    () ->{
                        return callback.success || callback.intercept ;
                    });

            System.out.println(callback.getoutput());
            logger.info("Returning the result");
            return callback.getoutput();
        } catch (Exception ex){
            logger.error("Exception occured ..");
        }

        return "Hello for now";
    }

    /**
     * //kubectl exec jdoodle-deploy-6b6cf6459-s862q -- curl -s http://localhost
     * @return
     */
    public String getPagek8s(){
        Exec exec = new Exec();
        final Process proc;
        try {
            CoreV1Api api = new CoreV1Api();
            V1PodList list = api.listNamespacedPod(AppConstants.NAMESPACE, null, null, null, null, null, null, null, null, null, null);
            if (list.getItems().size() == 0){
                return "No pods created or running";
            }

            String podName = list.getItems().get(0).getMetadata().getName();
            proc = exec.exec(AppConstants.NAMESPACE, podName
                    //"jdoodle-deploy-6b6cf6459-m7p77"
                    , new String[] {"curl","-s","http://localhost"}
                    , false, false);
        } catch (ApiException|IOException e) {
            return "Container with spcified image not running";
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Streams.copy(proc.getInputStream(), outputStream);
            proc.waitFor();
            return outputStream.toString();
        } catch (IOException|InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private class CallBackForOutput implements ApiCallback{

        String output = "hello";
        boolean success, intercept = false;

        @Override
        public void onFailure(ApiException e, int statusCode, Map responseHeaders) {
            logger.error("Recieved error:{}", e);
            intercept = true;
            this.output = "Failure to read the data from the exec command";
        }

        @Override
        public void onSuccess(Object result, int statusCode, Map responseHeaders) {
            this.output = result.toString();
            logger.info("Got the response from the exe {}", result.toString());
            intercept = false;
            success = true;
        }

        @Override
        public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
            logger.info("upload is in progress");
        }

        @Override
        public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
            logger.info("Download is in progress");
        }

        public String getoutput(){
            return this.output;
        }
    }


}


package com.kubernatesconnect.kubernatesconnect.client;

import com.kubernatesconnect.kubernatesconnect.constants.AppConstants;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.extended.wait.Wait;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.PatchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;

public final class StartDeploymentKubernates {

    private final static Logger logger = LoggerFactory.getLogger(StartDeploymentKubernates.class);
    private ApiClient apiclient  = null;
    public StartDeploymentKubernates(ApiClient apiclient){
        this.apiclient = apiclient;
    }

    /*
     **  https://github.com/kubernetes-client/java/blob/automated-release-17.0.0/examples/examples-release-15/src/main/java/io/kubernetes/client/examples/DeployRolloutRestartExample.java
     */
    public boolean startKubernatesDeployment() throws ApiException {
        AppsV1Api appsV1Api = new AppsV1Api(this.apiclient);

        // Create an example deployment
        V1DeploymentBuilder deploymentBuilder = new V1DeploymentBuilder()
                .withApiVersion("apps/v1")
                .withKind("Deployment")
                .withMetadata(new V1ObjectMeta().name(AppConstants.DEPLOYMENTNAME).namespace(AppConstants.NAMESPACE))
                .withSpec(
                        new V1DeploymentSpec()
                                .replicas(1)
                                .selector(new V1LabelSelector().putMatchLabelsItem("name", AppConstants.DEPLOYMENTNAME))
                                .template(
                                        new V1PodTemplateSpec()
                                                .metadata(new V1ObjectMeta().putLabelsItem("name", AppConstants.DEPLOYMENTNAME))
                                                .spec(
                                                        new V1PodSpec()
                                                                .containers(
                                                                        Collections.singletonList(
                                                                                new V1Container()
                                                                                        .name(AppConstants.DEPLOYMENTNAME)
                                                                                        .image(AppConstants.IMAGENAME)
                                                                                        .imagePullPolicy("Never"))))));
        logger.info("Firing the deployment command");
        appsV1Api.createNamespacedDeployment(
                AppConstants.NAMESPACE, deploymentBuilder.build(), null, null, null);
        // Wait until example deployment is ready
        Wait.poll(
                Duration.ofSeconds(3),
                Duration.ofSeconds(60),
                () -> {
                    try {
                        System.out.println("Waiting until example deployment is ready...");
                        return appsV1Api
                                .readNamespacedDeployment(AppConstants.DEPLOYMENTNAME, AppConstants.NAMESPACE, null, null, null)
                                .getStatus()
                                .getReadyReplicas()
                                > 0;
                    } catch (ApiException e) {
                        e.printStackTrace();
                        return false;
                    }
                });
        logger.info("Created jdoodle deployment!");

        // Trigger a rollout restart of the example deployment
        V1Deployment runningDeployment =
                appsV1Api.readNamespacedDeployment(AppConstants.DEPLOYMENTNAME, AppConstants.NAMESPACE, null, null, null);

        // Explicitly set "restartedAt" annotation with current date/time to trigger rollout when patch
        // is applied
        runningDeployment
                .getSpec()
                .getTemplate()
                .getMetadata()
                .putAnnotationsItem("kubectl.kubernetes.io/restartedAt", LocalDateTime.now().toString());
        try {
            String deploymentJson = this.apiclient.getJSON().serialize(runningDeployment);

            PatchUtils.patch(
                    V1Deployment.class,
                    () ->
                            appsV1Api.patchNamespacedDeploymentCall(
                                    AppConstants.DEPLOYMENTNAME,
                                    AppConstants.NAMESPACE,
                                    new V1Patch(deploymentJson),
                                    null,
                                    null,
                                    "kubectl-rollout",
                                    null,
                                    null
                            ),
                    V1Patch.PATCH_FORMAT_STRATEGIC_MERGE_PATCH,
                    this.apiclient);

            // Wait until deployment has stabilized after rollout restart
            Wait.poll(
                    Duration.ofSeconds(3),
                    Duration.ofSeconds(60),
                    () -> {
                        try {
                            System.out.println("Waiting until example deployment restarted successfully...");
                            logger.info("Waiting until example deployment restarted successfully...");
                            return appsV1Api
                                    .readNamespacedDeployment(AppConstants.DEPLOYMENTNAME, AppConstants.NAMESPACE, null, null, null)
                                    .getStatus()
                                    .getReadyReplicas()
                                    > 0;
                        } catch (ApiException e) {
                            e.printStackTrace();
                            return false;
                        }
                    });
            logger.info("Jdoodle deployment restarted successfully!");
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return true;
    }
}

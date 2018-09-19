package io.elastest.etm.service.client;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.epm.client.service.DockerComposeService;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.TssManifest;
import io.elastest.etm.service.EtPluginsService;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;

public class EtmMiniSupportServiceClient
        implements SupportServiceClientInterface {
    final Logger logger = getLogger(lookup().lookupClass());

    @Value("${et.shared.folder}")
    private String sharedFolder;

    @Value("${elastest.docker.network}")
    private String etDockerNetwork;

    Map<String, SupportService> supportServicesMap;
    Map<String, TssManifest> tssManifestMap;

    DockerComposeService dockerComposeService;
    EtPluginsService etPluginsService;

    UtilsService utilsService;

    public EtmMiniSupportServiceClient(
            DockerComposeService dockerComposeService,
            EtPluginsService etPluginsService, UtilsService utilsService) {
        this.dockerComposeService = dockerComposeService;
        this.etPluginsService = etPluginsService;
        this.utilsService = utilsService;
        supportServicesMap = new HashMap<>();
        tssManifestMap = new HashMap<>();
    }

    // @PreDestroy
    // void destroy() {
    // if (!supportServiceInstanceMap.isEmpty()) {
    // for (Entry<String, SupportServiceInstance> instance :
    // supportServiceInstanceMap
    // .entrySet()) {
    // this.deprovisionServiceInstance(instance.getKey(),
    // instance.getValue());
    // }
    // }
    // }

    @Override
    public void registerService(String serviceRegistry) {
        SupportService tss = UtilTools.convertJsonStringToObj(serviceRegistry,
                SupportService.class, Include.NON_EMPTY);
        supportServicesMap.put(tss.getId(), tss);
    }

    @Override
    public void registerManifest(String serviceManifest, String id) {
        TssManifest manifest = UtilTools.convertJsonStringToObj(serviceManifest,
                TssManifest.class, Include.NON_EMPTY);
        supportServicesMap.get(manifest.getServiceId()).setManifest(manifest);
        tssManifestMap.put(id, manifest);
    }

    @Override
    public String provisionServiceInstance(
            SupportServiceInstance serviceInstance, String instanceId,
            String accept_incomplete) {
        TssManifest manifest = supportServicesMap
                .get(serviceInstance.getService_id()).getManifest();
        String composeYml = manifest.getManifestContent();

        composeYml.replaceAll("\\\\n", "\\n");

        try {
            etPluginsService.createTssInstanceProject(instanceId, composeYml,
                    serviceInstance);
            etPluginsService.startEtPlugin(instanceId);
        } catch (Exception e) {
            throw new RuntimeException("Exception provisioning service \""
                    + serviceInstance.getService_id() + "\" with instanceId \""
                    + instanceId + "\"", e);
        }

        return "";
    }

    @Override
    @Async
    public void deprovisionServiceInstance(String instanceId,
            SupportServiceInstance serviceInstance) {
        if (etPluginsService.stopAndRemoveProject(instanceId)) {
            logger.info("Service {} deprovisioned.",
                    serviceInstance.getServiceName());
        } else {
            throw new RuntimeException(
                    "Exception deprovisioning instance \"" + instanceId + "\"");
        }
    }

    @Override
    public SupportService[] getRegisteredServices() {
        return supportServicesMap.values().toArray(new SupportService[0]);
    }

    @Override
    public JsonNode getRawRegisteredServices() throws IOException {
        return UtilTools.convertObjToJsonNode(supportServicesMap.values());
    }

    @Override
    public JsonNode getRawServiceById(String serviceId) throws IOException {
        JsonNode service = null;
        JsonNode services = getRawRegisteredServices();
        for (JsonNode currentService : services) {
            if (currentService.get("id").toString().replaceAll("\"", "")
                    .equals(serviceId)) {
                service = currentService;
                break;
            }
        }
        return service;
    }

    @Override
    public SupportServiceInstance getServiceInstanceInfo(
            SupportServiceInstance instance) {
        return initSupportServiceInstanceData(
                (SupportServiceInstance) etPluginsService
                        .getEtPlugin(instance.getInstanceId()));
    }

    @Override
    public TssManifest getManifestById(String manifestId) {
        return new TssManifest(tssManifestMap.get(manifestId));
    }

    @Override
    public TssManifest getManifestBySupportServiceInstance(
            SupportServiceInstance serviceInstance) {
        return supportServicesMap.containsKey(serviceInstance.getService_id())
                ? new TssManifest(supportServicesMap
                        .get(serviceInstance.getService_id()).getManifest())
                : null;
    }

    @Override
    public SupportServiceInstance initSupportServiceInstanceData(
            SupportServiceInstance serviceInstance) {
        TssManifest manifest = supportServicesMap
                .get(serviceInstance.getService_id()).getManifest();
        serviceInstance.setManifestId(manifest.getId());
        initTssInstanceContainerName(serviceInstance);

        try {
            String containerIp = dockerComposeService.dockerService
                    .getContainerIp(serviceInstance.getContainerName(),
                            etDockerNetwork);
            serviceInstance.setContainerIp(containerIp);
            logger.info("ET_PUBLIC_HOST value: " + utilsService.getEtPublicHostValue());
            String serviceIp = !utilsService.isDefaultEtPublicHost()
                            ? utilsService.getEtPublicHostValue()
                            : containerIp;
            serviceInstance.setServiceIp(serviceIp);
        } catch (Exception e) {
            logger.warn("Error on getting TSS instance container ip", e);
        }

        return serviceInstance;
    }

    public void initTssInstanceContainerName(
            SupportServiceInstance serviceInstance) {
        if (serviceInstance.getContainerName() == null) {
            TssManifest manifest = supportServicesMap
                    .get(serviceInstance.getService_id()).getManifest();

            if (manifest != null && manifest.getEndpoints() != null) {

                if (manifest.getEndpoints().fieldNames().hasNext()) {
                    String containerName = serviceInstance.getInstanceId() + "_"
                            + manifest.getEndpoints().fieldNames().next()
                            + "_1";
                    serviceInstance.setContainerName(containerName);

                }
            }
        }

    }

    @Override
    public ObjectNode getServiceInstanceInfo(String instanceId) {
        return UtilTools
                .convertObjToObjectNode(getServiceInstanceInfo(instanceId));
    }

}

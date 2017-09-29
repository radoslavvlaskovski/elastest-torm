package io.elastest.etm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SupportServiceInstance {

	public interface ProvisionView {
	}

	public interface FrontView {
	}

	@Value("${et.edm.alluxio.api}")
	public String ET_EDM_ALLUXIO_API;
    	   
	@JsonView(FrontView.class)
	private String instanceId;
	@JsonView({ ProvisionView.class, FrontView.class })
	private String service_id;
	
	@JsonView(FrontView.class)
	private String serviceName;
	
	@JsonView(FrontView.class)
	private String serviceShortName;
	
	@JsonView(ProvisionView.class)
	private String plan_id;
	@JsonView(ProvisionView.class)
	private String organization_guid;
	@JsonView(ProvisionView.class)
	private Map<String, String> parameters;
	@JsonView(ProvisionView.class)
	private Map<String, String> context;
	@JsonView(ProvisionView.class)
	private String space_guid;

	private Long tJobExecId;
	@JsonView(FrontView.class)
	private String serviceIp;
	@JsonView(FrontView.class)
	private int servicePort;
	private String containerIp;

	private String manifestId;

	@JsonView(FrontView.class)
	private Map<String, String> urls;
	@JsonView(FrontView.class)
	private List<SupportServiceInstance> subServices;

	@JsonView(FrontView.class)
	private String endpointName;

	@JsonView(FrontView.class)
	private Map<String, JsonNode> endpointsData;

	private List<String> portBindingContainers;

	public SupportServiceInstance() {
		this.urls = new HashMap<>();
		this.subServices = new ArrayList<>();
		this.endpointsData = new HashMap<>();
		this.portBindingContainers = new ArrayList<>();
	}

	public SupportServiceInstance(String instanceId, String service_id, String serviceName, String serviceShortName, String plan_id, Long bindToTJob) {
		super();
		this.instanceId = instanceId;
		this.service_id = service_id;
		this.serviceName = serviceName;
		this.serviceShortName = serviceShortName;
		this.plan_id = plan_id;
		this.tJobExecId = bindToTJob;
		this.parameters = new HashMap<>();
		this.context = new HashMap<>();
		this.organization_guid = "org";
		this.space_guid = "space";
		this.manifestId = "";
		this.urls = new HashMap<>();
		this.subServices = new ArrayList<>();
		this.endpointsData = new HashMap<>();
		this.portBindingContainers = new ArrayList<>();
	}

	public SupportServiceInstance(String instanceId, String service_id, String serviceName, String serviceShortName, String plan_id, String organization_guid,
			Map<String, String> parameters, String space_guid, Long bindedToTJob, String serviceIp, int servicePort,
			String manifestId, Map<String, String> urls, List<SupportServiceInstance> subServices, String containerIp) {
		super();
		this.instanceId = instanceId;
		this.service_id = service_id;
		this.serviceName = serviceName;
		this.serviceShortName = serviceShortName;
		this.plan_id = plan_id;
		this.organization_guid = organization_guid;
		this.parameters = parameters;
		this.space_guid = space_guid;
		this.tJobExecId = bindedToTJob;
		this.serviceIp = serviceIp;
		this.servicePort = servicePort;
		this.manifestId = manifestId;
		this.urls = urls;
		this.subServices = subServices;
		this.containerIp = containerIp;
	}

	public String getService_id() {
		return service_id;
	}

	public void setService_id(String service_id) {
		this.service_id = service_id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceShortName() {
		return serviceShortName;
	}

	public void setServiceShortName(String serviceShortName) {
		this.serviceShortName = serviceShortName;
	}

	public String getPlan_id() {
		return plan_id;
	}

	public void setPlan_id(String plan_id) {
		this.plan_id = plan_id;
	}

	public String getOrganization_guid() {
		return organization_guid;
	}

	public void setOrganization_guid(String organization_guid) {
		this.organization_guid = organization_guid;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getSpace_guid() {
		return space_guid;
	}

	public Long gettJobExecId() {
		return tJobExecId;
	}

	public void settJobExecId(Long tJobExecId) {
		this.tJobExecId = tJobExecId;
	}

	public void setSpace_guid(String space_guid) {
		this.space_guid = space_guid;
	}

	public String getServiceIp() {
		return serviceIp;
	}

	public void setServiceIp(String serviceIp) {
		this.serviceIp = serviceIp;
	}

	public int getServicePort() {
		return servicePort;
	}

	public void setServicePort(int servicePort) {
		this.servicePort = servicePort;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getManifestId() {
		return manifestId;
	}

	public void setManifestId(String manifestId) {
		this.manifestId = manifestId;
	}

	public List<SupportServiceInstance> getSubServices() {
		return subServices;
	}

	public void setSubServices(List<SupportServiceInstance> subServices) {
		this.subServices = subServices;
	}

	public Map<String, String> getUrls() {
		return urls;
	}

	public void setUrls(Map<String, String> urls) {
		this.urls = urls;
	}

	public String getEndpointName() {
		return endpointName;
	}

	public void setEndpointName(String endpointName) {
		this.endpointName = endpointName;
	}

	public Map<String, JsonNode> getEndpointsData() {
		return endpointsData;
	}

	public void setEndpointsData(Map<String, JsonNode> endpointsData) {
		this.endpointsData = endpointsData;
	}

	public List<String> getPortBindingContainers() {
		return portBindingContainers;
	}

	public void setPortBindingContainers(List<String> portBindingContainers) {
		this.portBindingContainers = portBindingContainers;
	}

	public String getContainerIp() {
		return containerIp;
	}

	public void setContainerIp(String containerIp) {
		this.containerIp = containerIp;
	}

	public Map<String, String> getContext() {
		return context;
	}

	public void setContext(Map<String, String> context) {
		this.context = context;
	}

}

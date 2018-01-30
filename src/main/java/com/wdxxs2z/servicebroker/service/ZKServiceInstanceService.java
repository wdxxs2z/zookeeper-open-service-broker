package com.wdxxs2z.servicebroker.service;

import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Component;

import com.wdxxs2z.servicebroker.configuration.client.ZKServiceAdmin;

@Component
public class ZKServiceInstanceService implements ServiceInstanceService {
	
	private Log log = LogFactory.getLog(ZKServiceInstanceService.class);
	
	@Autowired
	private ZKServiceAdmin zkServiceAdmin;

	@Override
	public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
		
		String serviceInstanceId = request.getServiceInstanceId();
		String username = null;
		String password = null;
		String digest = "";
		
		if (request.getParameters() != null && request.getParameters().containsKey("username") && request.getParameters().containsKey("password")) {
			username = (String) request.getParameters().get("username");
			password = (String) request.getParameters().get("password");
		}else {
			throw new ServiceBrokerInvalidParametersException("usernmae and password parameter must be set.");
		}
		
		try {
			digest = DigestAuthenticationProvider.generateDigest(username + ":" + password);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		log.info("Creating Service Instance({ " + serviceInstanceId + " })");
		if (zkServiceAdmin.databaseExistsWithAdmin(serviceInstanceId)) {
			throw new ServiceInstanceExistsException(serviceInstanceId,
			           request.getServiceDefinitionId());
		}
		
		zkServiceAdmin.createDatabase("/" + serviceInstanceId, username + ":" + password);
		log.info("Created Service Instance({ " + serviceInstanceId + " })");
		
		zkServiceAdmin.createDatabaseWithAdmin(serviceInstanceId);
		zkServiceAdmin.createAuthDataWithAdmin(serviceInstanceId, digest);
		return new CreateServiceInstanceResponse();
	}

	@Override
	public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
		String serviceInstanceId = request.getServiceInstanceId();
        log.info("Get Last Service Operation({ " + serviceInstanceId + "})");
        return new GetLastServiceOperationResponse();
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
		String serviceInstanceId = request.getServiceInstanceId();
        log.info("Deleting Service Instance({ " + serviceInstanceId + "})");
        if (!zkServiceAdmin.databaseExistsWithAdmin(serviceInstanceId)) {
        	throw new ServiceInstanceDoesNotExistException(serviceInstanceId);
		}
        zkServiceAdmin.deleteDatabase("/" + serviceInstanceId);
        zkServiceAdmin.deleteDatabaseWithAdmin(serviceInstanceId);
        log.info("Deleted Service Instance({ " + serviceInstanceId + "})");
        return new DeleteServiceInstanceResponse();
	}

	@Override
	public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
		String serviceInstanceId = request.getServiceInstanceId();
        log.info("Update Service Instance({ " + serviceInstanceId + "})");
        return new UpdateServiceInstanceResponse();
	}

}

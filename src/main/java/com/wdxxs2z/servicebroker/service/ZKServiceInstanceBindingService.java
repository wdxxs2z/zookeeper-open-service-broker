package com.wdxxs2z.servicebroker.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Component;

import com.wdxxs2z.servicebroker.configuration.client.ZKServiceAdmin;

@Component
public class ZKServiceInstanceBindingService implements ServiceInstanceBindingService {
	
	private Log log = LogFactory.getLog(ZKServiceInstanceBindingService.class);
	
	@Autowired
	private ZKServiceAdmin zkServiceAdmin;

	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest request) {
		
		String serviceInstanceId = request.getServiceInstanceId();
        String bindingId = request.getBindingId();
        
        log.info("Creating Service Binding({ " + bindingId + "})" + " for Service({ " + serviceInstanceId + " })");
        
        if (zkServiceAdmin.databaseExistsWithAdmin(serviceInstanceId + "/" + bindingId)) {
        	throw new ServiceInstanceBindingExistsException(serviceInstanceId,
        	        bindingId);
        }
        
        Map<String, Object> credentials = new HashMap<String, Object>();
        String connectionString = zkServiceAdmin.getConnectionString("/" + serviceInstanceId);
        String digest = zkServiceAdmin.getAuthDataWithAdmin(serviceInstanceId);

        credentials.put("zookeeper_hosts", connectionString);
        credentials.put("zookeeper_data_dir", "/" + serviceInstanceId);
        credentials.put("auth", digest);
        
        zkServiceAdmin.createDatabaseWithAdmin(serviceInstanceId + "/" + bindingId);
        
        log.info("Created Service Binding({ " + bindingId + "})" + " for Service({ " + serviceInstanceId + " })");
        
        return new CreateServiceInstanceAppBindingResponse().withCredentials(credentials);
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
		String serviceInstanceId = request.getServiceInstanceId();
        String bindingId = request.getBindingId();
        if (zkServiceAdmin.databaseExistsWithAdmin(serviceInstanceId + "/" + bindingId)) {
        	zkServiceAdmin.deleteDatabaseWithAdmin(serviceInstanceId + "/" + bindingId);
            log.info("Deleted Service Binding({ " + " })" + bindingId + " for Service({ " + serviceInstanceId + " })");
        }       
	}
}

package com.wdxxs2z.servicebroker.configuration.client;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZookeeperClientConfig {
	
	@Value("${zk.hosts:127.0.0.1:2181}")
	private String hosts;
	
	@Value("${zk.adminauth:super:admin}")
	private String adminauth;
	
	private static final String AUTH_TYPE = "digest";
	
	private static final int SESSION_TIMEOUT = 15000;
	
	@Bean
	public CuratorFramework zkClient() {
		RetryPolicy policy = new ExponentialBackoffRetry(1000, 10);
		CuratorFramework zkClient = CuratorFrameworkFactory.builder()
				.connectString(hosts)
				.authorization(AUTH_TYPE, adminauth.getBytes())
				.sessionTimeoutMs(SESSION_TIMEOUT)
				.retryPolicy(policy).build();
		return zkClient;
	}

}

package com.wdxxs2z.servicebroker.configuration.client;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class ZKServiceAdmin {
	
	public static final String ADMIN_DB = "/zookeeper-service/";
	
	public static final String AUTH_TYPE = "digest";
	
	@Value("${zk.adminauth:super:admin}")
	private String adminauth;
	
	@Autowired
	private CuratorFramework zkClient;
	
	public ZKServiceAdmin(CuratorFramework zkClient) {
		this.zkClient = zkClient;
		zkClient.start();
	}
	
	public boolean databaseExistsWithAdmin(String databaseName) {
		return databaseExists(ADMIN_DB + databaseName);
	}
	
	public boolean databaseExists(String databaseName) {
		Stat stat;
		try {
			stat = zkClient.checkExists().forPath(databaseName);
			if (stat == null) {
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void deleteDatabaseWithAdmin(String databaseName) {
		deleteDatabase(ADMIN_DB + databaseName);
	}
	
	public void deleteDatabase(String databaseName){
		try {
			zkClient.delete()
			.guaranteed().
			deletingChildrenIfNeeded()
			.forPath(databaseName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createAuthDataWithAdmin(String databaseName, String auth) {
		try {
			createDatabaseWithAdmin(databaseName + "/auth");
			zkClient.setData().forPath(ADMIN_DB + databaseName + "/auth", auth.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getAuthDataWithAdmin(String databaseName) {
		String auth = "";
		try {
			auth = new String(zkClient.getData().forPath(ADMIN_DB + databaseName + "/auth"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return auth;
	}
	
	public String createDatabaseWithAdmin(String databaseName) {
		return createDatabase(ADMIN_DB + databaseName, adminauth);
	}
	
	public String createDatabase(String databaseName, String auth) {
		String path = "";
		try {
			if (auth == null) {
				path = zkClient.create()
						.creatingParentsIfNeeded()
						.withMode(CreateMode.PERSISTENT)
						.forPath(databaseName);
			}else {
				path = zkClient.create()
						.creatingParentsIfNeeded()
						.withMode(CreateMode.PERSISTENT)
						.withACL(createPermAuth(auth))
						.forPath(databaseName);
			}
		} catch (Exception e) {
			try {
				deleteDatabase(databaseName);
			} catch (Exception ignore) {
				ignore.printStackTrace();
			}
		}
		return path;		
	}
	
	public String getConnectionString(String database) {
		return new StringBuilder()
				.append(getServerAddresses())
				.toString();				
	}

	private Object getServerAddresses() {
		StringBuilder builder = new StringBuilder();
		builder.append(zkClient.getZookeeperClient().getCurrentConnectionString());
		return builder.toString();
	}
	
	private List<ACL> createPermAuth(String auth) {
		List<ACL> acls = new ArrayList<ACL>(1);
		ACL acl = null;	
		try {
			Id id = new Id(AUTH_TYPE, DigestAuthenticationProvider.generateDigest(auth));
			acl = new ACL(ZooDefs.Perms.ALL, id);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		acls.add(acl);		
		return acls;		
	}

}

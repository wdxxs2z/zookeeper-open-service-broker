# Zookeeper Service Broker Example

## Configuration

1.Make sure your zookeeper open digest auth: super:admin
```
"-Dzookeeper.DigestAuthenticationProvider.superDigest=super:xQJmxLMiHGwaqBvst5y6rkB6HQs="
```
2.Push our zookeeper to cloudfoundry or kubernetes.
```
cf push zk-broker --no-start
cf set-env ZK_HOSTS 127.0.0.1:2181
cf set-env ZK_SUPER_AUTH super:admin
cf set-env SERVICE_BROKER_USERNAME admin
cf set-env SERVICE_BROKER_PASSWORD changeme
cf start zk-broker
```
3.Create service-broker and access the free plan
```
cf create-service-broker zookeeper-service-broker admin changeme http://zk-broker.your.domain
cf enable-service-access zookeeper-service-broker free
```
4.Create zookeeper service instance with parm
```
cf create-service zookeeper-service-broker free zk-example -c "{"username": "myuser","password": "mypass"}"
```
5.Bind the service to application
```
cf bind-service app_name zk-example
```
6.Check your application environment
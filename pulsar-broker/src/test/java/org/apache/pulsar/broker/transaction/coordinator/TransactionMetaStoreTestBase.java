/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.broker.transaction.coordinator;

import org.apache.bookkeeper.test.PortManager;
import org.apache.pulsar.PulsarTransactionCoordinatorMetadataSetup;
import org.apache.pulsar.broker.NoOpShutdownService;
import org.apache.pulsar.broker.PulsarService;
import org.apache.pulsar.broker.ServiceConfiguration;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.transaction.TransactionCoordinatorClient;
import org.apache.pulsar.client.impl.transaction.TransactionCoordinatorClientImpl;
import org.apache.pulsar.zookeeper.LocalBookkeeperEnsemble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

import java.net.URL;
import java.util.Optional;

public class TransactionMetaStoreTestBase {

    private static final Logger log = LoggerFactory.getLogger(TransactionMetaStoreTestBase.class);

    LocalBookkeeperEnsemble bkEnsemble;
    protected final int ZOOKEEPER_PORT = PortManager.nextFreePort();
    protected int[] brokerWebServicePorts = new int[BROKER_COUNT];
    protected int[] brokerNativeBrokerPorts = new int[BROKER_COUNT];
    protected URL[] brokerUrls = new URL[BROKER_COUNT];
    protected PulsarAdmin[] pulsarAdmins = new PulsarAdmin[BROKER_COUNT];
    protected static final int BROKER_COUNT = 5;
    protected ServiceConfiguration[] configurations = new ServiceConfiguration[BROKER_COUNT];
    protected PulsarService[] pulsarServices = new PulsarService[BROKER_COUNT];

    protected TransactionCoordinatorClient transactionCoordinatorClient;

    @BeforeClass
    void setup() throws Exception {
        log.info("---- Initializing SLAMonitoringTest -----");
        // Start local bookkeeper ensemble
        bkEnsemble = new LocalBookkeeperEnsemble(3, ZOOKEEPER_PORT, () -> PortManager.nextFreePort());
        bkEnsemble.start();

        String[] args = new String[]{
            "--cluster", "my-cluster",
            "--configuration-store", "localhost:" + ZOOKEEPER_PORT,
            "--initial-num-transaction-coordinators", "16"};

        PulsarTransactionCoordinatorMetadataSetup.main(args);

        // start brokers
        for (int i = 0; i < BROKER_COUNT; i++) {
            brokerWebServicePorts[i] = PortManager.nextFreePort();
            brokerNativeBrokerPorts[i] = PortManager.nextFreePort();

            ServiceConfiguration config = new ServiceConfiguration();
            config.setBrokerServicePort(Optional.ofNullable(brokerNativeBrokerPorts[i]));
            config.setClusterName("my-cluster");
            config.setAdvertisedAddress("localhost");
            config.setWebServicePort(Optional.ofNullable(brokerWebServicePorts[i]));
            config.setZookeeperServers("127.0.0.1" + ":" + ZOOKEEPER_PORT);
            config.setBrokerServicePort(Optional.ofNullable(brokerNativeBrokerPorts[i]));
            config.setDefaultNumberOfNamespaceBundles(1);
            config.setLoadBalancerEnabled(false);
            configurations[i] = config;

            pulsarServices[i] = new PulsarService(config);
            pulsarServices[i].setShutdownService(new NoOpShutdownService());
            pulsarServices[i].start();

            brokerUrls[i] = new URL("http://127.0.0.1" + ":" + brokerWebServicePorts[i]);
            pulsarAdmins[i] = PulsarAdmin.builder().serviceHttpUrl(brokerUrls[i].toString()).build();
        }

        Thread.sleep(100);

        PulsarClient client = PulsarClient.builder().
            serviceUrl(pulsarServices[0].getBrokerServiceUrl())
            .build();
        transactionCoordinatorClient = new TransactionCoordinatorClientImpl(client);
        transactionCoordinatorClient.start();

        Thread.sleep(3000);
    }
}

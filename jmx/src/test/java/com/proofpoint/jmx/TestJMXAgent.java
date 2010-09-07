package com.proofpoint.jmx;

import com.proofpoint.net.NetUtils;
import org.testng.annotations.Test;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import static org.testng.Assert.assertTrue;

public class TestJMXAgent
{
    @Test
    public void testAuto()
            throws IOException
    {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        JMXAgent agent = new JMXAgent(server, new JMXConfig());
        agent.start();

        JMXConnector connector = JMXConnectorFactory.connect(agent.getURL());
        connector.connect();

        MBeanServerConnection connection = connector.getMBeanServerConnection();
        assertTrue(connection.getMBeanCount() > 0);
    }

    @Test
    public void testSpecificHost()
            throws IOException
    {
        final String host = Inet4Address.getLocalHost().getCanonicalHostName();

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        JMXAgent agent = new JMXAgent(server, new JMXConfig() {
            @Override
            public String getRmiServerHostname()
            {
                return host;
            }
        });
        agent.start();

        JMXConnector connector = JMXConnectorFactory.connect(agent.getURL());
        connector.connect();

        MBeanServerConnection connection = connector.getMBeanServerConnection();
        assertTrue(connection.getMBeanCount() > 0);
    }

    @Test
    public void testSpecificHostAndPort()
            throws IOException
    {
        final String host = Inet4Address.getLocalHost().getCanonicalHostName();
        final int port = NetUtils.findUnusedPort();

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        JMXAgent agent = new JMXAgent(server, new JMXConfig() {
            @Override
            public Integer getRmiRegistryPort()
            {
                return port;
            }

            @Override
            public String getRmiServerHostname()
            {
                return host;
            }
        });
        agent.start();

        JMXConnector connector = JMXConnectorFactory.connect(agent.getURL());
        connector.connect();

        MBeanServerConnection connection = connector.getMBeanServerConnection();
        assertTrue(connection.getMBeanCount() > 0);
    }

}

# Coherence VisualVM Plugin

![Coherence CE](https://oracle.github.io/coherence/assets/images/logo-red.png)

![CI Build](https://github.com/oracle/coherence-visualvm/workflows/Java%20CI%20-%20Released%20versions/badge.svg)
![Release](https://img.shields.io/github/v/release/oracle/coherence-visualvm)
![Sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=oracle_coherence-visualvm&metric=alert_status)

The Coherence-VisualVM Plugin (the Plugin) provides management and monitoring of a single Coherence cluster using the VisualVM management utility.

The Plugin aggregates Coherence MBean data and shows a concise operational view of a single Coherence cluster.
Some management information is presented over time, which allows real-time analysis and troubleshooting.
You can connect to clusters via JMX or via management over REST with Coherence versions 12.2.1.4 or above.

The Plugin is an ideal tool for monitoring and managing Coherence clusters during the development and testing lifecycle and supports connecting to both
Community Edition and Commercial versions of Coherence.

NOTE: The most current version of the Plugin requires VisualVM release 2.1 or later which is available from https://visualvm.github.io/.

![Coherence VisualVM Plugin](assets/coherence-visualvm.png)

# Table of Contents

1. [Supported Coherence Versions](#versions)
2. [Installing the Plugin from VisualVM](#install)
3. [Connecting to a Coherence Cluster](#connect)
4. [Changing the Plugin Behaviour via the Options Tab](#prefs)
5. [Monitoring Capabilities](#capabilities)
6. [Using Coherence with the Tracer framework](#tracer)
7. [Building the Plugin](#build)

## <a id="versions"></a> Supported Coherence Versions

The Plugin will connect to and display data for the following Coherence versions:

* **Community Editions**: 24.03.x, 23.09.x, 22.06.x, 21.12.x (*), 14.1.1.0.x

* **Commercial Editions**: 14.1.1.2206.x, 14.1.1.0.x, 12.2.1.5.x, 12.2.1.4.x 12.1.3.x and 12.1.2.x

>Note: If you wish to connect to Coherence version 12.2.1.4.x via REST you should have Coherence version 12.2.1.4.7 or greater.

>Note: Support for versions older than Commercial Edition 3.7, and Community Edition 21.12 is now deprecated and no longer supported. The plugin may still work with these older versions, but is not guaranteed to continue to. Please upgrade to the latest Coherence versions available for continued support.


## <a id="install"></a> Installing the Plugin from VisualVM

The Coherence VisualVM Plugin is available from the list of plugins in VisualVM versions 2.0.6 and above.

>Note: It is recommended to install VisualVM version 2.1.5 or later. This will ensure you can install the most up-to-date Coherence Plugin.

To install the Plugin carry out the following:

1. Choose `Tools` -> `Plugins` from the main menu.
2. The Plugin will be displayed as `VisualVM-Coherence`. If it is not present in the list then click on the `Check for Newest` button
3. In the `Available Plugins` tab, select the Install checkbox for the `VisualVM-Coherence`. Click Install.
4. Step through and complete the plugin installer.

![Coherence VisualVM Plugin Install](assets/coherence-visualvm-install.png)

>Note: If you already have an older version of the plugin installed, click on `Check For Newest` and follow the prompts.

For more information about using the Coherence VisualVM plugin see the official [Coherence Documentation](https://docs.oracle.com/en/middleware/standalone/coherence/14.1.1.2206/manage/using-jmx-manage-oracle-coherence.html).

Other useful resources:

* [The Coherence Community - All things Coherence](https://coherence.community/)
* [VisualVM Home Page](https://visualvm.github.io/)
* [Coherence Community Edition on GitHub](https://github.com/oracle/coherence)
* [Various Coherence Examples](https://coherence.community/latest/22.09/docs/#/examples/guides/000-overview)
* [The Coherence Operator - Run your clusters in Kubernetes](https://github.com/oracle/coherence-operator)


## <a id="connect"></a> Connecting to a Coherence Cluster

### 1. Connecting Directly to a Process

Once the Plugin is installed, you can double-click on a Coherence process in the left pane, usually `com.tangosol.net.DefaultCacheServer`, after which a `Coherence` tab will be displayed.

### 2. Connecting via Management over REST

You can also connect via Coherence Management over REST by right-clicking on the `Coherence Clusters` tree item and choose `Add Coherence Cluster`.

Provide a name for the cluster and use the following URL based upon what type of cluster you are connecting to:

1. Standalone Coherence - `http://<host>:<management-port>/management/coherence/cluster`

2. WebLogic Server -  `http://<admin-host>:<admin-port>/management/coherence/<version>/clusters` - You can use `latest` as the version.

>Note: To enable Management over REST for a stand-alone cluster, please see the
[Coherence Documentation](https://docs.oracle.com/en/middleware/standalone/coherence/14.1.1.2206/rest-reference/quick-start.html)

### 3. Connecting to Coherence in WebLogic Server via the Admin Server

If you have Coherence running within WebLogic Server using the `Managed Coherence Servers` functionality you can either
connect via REST as described above or if you want to connect to the `domain runtime MBean server`, use the instructions below.

1. Ensure you have the same version of WebLogic Server installed locally as the instance you are connecting to.

2. Use the following (on one line) to start VisualVM replacing WLS_HOME with your WebLogic Server home.

   ```bash
   /path/to/visualvm --cp WLS_HOME/server/lib/wljmxclient.jar:WLS_HOME/server/lib/weblogic.jar
      -J-Djmx.remote.protocol.provider.pkgs=weblogic.management.remote
      -J-Dcoherence.plugin.visualvm.disable.mbean.check=true
   ```

   >Note: On a Mac, the default VisualVM installed is usually `/Applications/VisualVM.app/Contents/MacOS/visualvm`.
   For Windows ensure that you use `visualvm.exe` and change the `/` to `\` and change the classpath separator from `:` to `;`.

3. From the VisualVM Applications tree, right-click `Local` and select `Add JMX Connection`. The Add JMX Connection dialog box displays.

4. Use either of the following connect strings depending upon the WebLogic Version you are connecting to.

   For WebLogic Server 14.1.1.X and above use **t3** protocol:

   ```bash
   service:jmx:t3://hostname:port/jndi/weblogic.management.mbeanservers.domainruntime
   ```
   
   For WebLogic Server 12.2.1.5 and below use **iiop** protocol:
   
   ```bash
   service:jmx:iiop://hostname:port/jndi/weblogic.management.mbeanservers.domainruntime
   ```

   >Note: in WebLogic Server 14.1.1.x and above the `wljmxclient.jar` no longer exists and will be ignored in the classpath.
   You may remove it from the above `--cp` statement if you like.

5. Click `Use security credentials` and enter the WebLogic Server username and password.

6. Check `Do not require SSL connection` if your connection is not SSL and select `Connect Immediately`.

7. Right-Click on the connection and select `Open`. The Coherence tab will be displayed.

   If you wish to secure access to the REST endpoints or via JMX, please refer to either the [Coherence Documentation](https://docs.oracle.com/en/middleware/standalone/coherence/14.1.1.2206/index.html) or relevant JMX security documentation.

## <a id="prefs"></a> Changing the Plugin Behaviour via the Options Tab

In the VisualVM Plugin, you can change the behaviour of the plugin
by using the Options pane. To open the options choose the following depending upon your platform:

1. Mac:  `VisualVM` -> `Preferences` and select the `Coherence` tab.

2. Windows/Linux: `Tools` -> `Options` and select the `Coherence` tab.

You will see the preferences as shown below:

![Coherence CE](assets/coherence-visualvm-preferences.png)

There are tool tips for each of the preferences, but a summary is shown below.

**Coherence VisualVM Preferences**

| Preference                          | Default | Usage                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|-------------------------------------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Data Refresh Time                   | 30      | Time (in seconds) between refreshing data from the cluster. Do not set too low as this could adversely affect performance in large clusters.                                                                                                                                                                                                                                                                                                           |
| Log Query Times                     | false   | Enables logging of query times to the VisualVM logfile when retrieving data.                                                                                                                                                                                                                                                                                                                                                                           |
| Disable MBean Check                 | false   | Disables the MBean check when connecting to WebLogic Server. This allows the plugin to startup without checking for Cluster MBean.                                                                                                                                                                                                                                                                                                                     |
| REST Request Timeout                | 30000   | The request timeout (in ms) when using REST to connect to a cluster.                                                                                                                                                                                                                                                                                                                                                                                   |
| Enable REST Debug                   | false   | Enables HTTP request debugging when using REST to connect to a cluster.                                                                                                                                                                                                                                                                                                                                                                                |
| Disable SSL Certificate Validation  | false   | If selected, will disable SSL certificate validation. Note: You should only use this option when you are sure of the identity of the target server.                                                                                                                                                                                                                                                                                                    |
| Enable Persistence List             | true    | Enables dropdown list of snapshots rather than having to enter the snapshot when performing snapshot operations.                                                                                                                                                                                                                                                                                                                                       |
| Enable Zoom on Graphs               | false   | Enables additional zoom function for all graphs.                                                                                                                                                                                                                                                                                                                                                                                                       |
| Enable Cluster Snapshot tab         | false   | Enables experimental Cluster Snapshot tab. This tab is useful for seeing all the relevant cluster information on one page in a text format.                                                                                                                                                                                                                                                                                                            |
| Enable Cluster Heap Dump            | false   | Enables the cluster heap dump button on the Cluster Overview tab.                                                                                                                                                                                                                                                                                                                                                                                      |
| Analyze Unavailable Time in LogFile |         | Provides the ability to analyze log files where Partition Events Logging has been enabled for logs generated from Coherence versions 21.06 and above. See [here](https://docs.oracle.com/pls/topic/lookup?ctx=en/middleware/standalone/coherence/14.1.1.2206/release-notes&id=COHDG-GUID-41F5341C-0318-41B2-AEBF-B9DB7FBF25E7) for more details. Note: You select a Coherence log file to analyze and don't need to be connected to a running cluster. |


## <a id="capabilities"></a> Monitoring Capabilities

For all Coherence clusters, the following tabs are displayed:

* **Cluster Overview** - Displays high-level information about the Coherence cluster including cluster name, version, member count and 'Cluster StatusHA'. Summary graphs show total cluster memory available and used, packet publisher and receiver success rates and load averages for machines running Coherence.
* **Machines** - Displays a list of the physical machines that make up the Coherence cluster as well as information about the load averages and available memory on these machines. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#MachineTableModel) for more details.
* **Members** - Displays the full list of Coherence members/nodes including individual publisher/ receiver success rates, memory and send queue sizes. See [here](https://github.com/oracle/coherence-visualvm/blob/main/helpr/help.adoc#MemberTableModel) for more details.
* **Services** - Displays information about the running services including partition counts and statusHA values. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#ServiceTableModel) for more details.
If you select a service, on the next data refresh you will see detailed thread information for each node of the service as well as
graphs of that information
* **Caches** - Displays information about any caches including size, and memory usage information. To get the correct information to be displayed for memory usage, you must be using the binary unit-calculator. If you select a cache, on the next data refresh you will see detailed information about each node hosting that service and cache. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#CacheTableModel) for more details.

Depending upon the edition and functionality you are using, the following optional tabs may be displayed:

* **Topics** - If you cluster is configured with topics, this tab displays information about any active Topics including size, message rates and unconsumed messages. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#TopcTableModel) for more details.
* **Proxy Servers**  - If your cluster is running proxy servers, this tab displays information about the proxy servers and the number of connections across each proxy server and total connections. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#ProxyTableModel) for more details.
* **HTTP Servers**  - If your cluster is running proxy servers with HTTP acceptors, this tab displays information about the HTTP servers, the number of connections across each server, total connections and graphs of response codes, errors and requests over time for a selected service. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#HttpProxyTableModel) for more details.
* **Executors** - If your cluster is configured to run the Executor Service, this tab displays information about the number of tasks completed, in-progress and rejected. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#ExecutorTableModel) for more details.
* **Coherence*Web** - If your cluster is configured for Coherence*Web, this tab displays information about the number applications deployed, the number of HTTP sessions being stored as well as other information regarding session reaping. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#HttpSessionTableModel) for more details.
* **Federation** - If your cluster is configured with Federated Caching, this tab displays information about each federated service. If you select a service, on the next data refresh you will see detailed outbound/inbound federation traffic information for each node of the service as well as graphs of that information. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#FederationTableModel) for more details.
* **Persistence** - If your cluster is configured with Persistence, this tab displays information about each service configured with Persistence.  Graphs showing active space used and any additional latencies incurred are also showed. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#PersistenceTableModel) for more details.
* **Elastic Data** - If your cluster is configured with Elastic Data, this tab displays graphs and information about RAM Journal and Flash Journal usage.  You can click on each of the usage bars to show detailed node information. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#ElasticData) for more details.
* **JCache** - If your cluster is being used to store JCache caches, this tab displays JCache "Management" and "Statistics" MBean information regarding the configured caches. See [help](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#JCacheConfigurationTableModel) for more details.
* **HotCache** - If your cluster contains HotCache node(s), then this tab lists the running HotCache instances. If you select an instance, on the next data refresh the console will display statistics and graphs for the operations performed. You may click on tabs and cache-ops to see further fine-grained information. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#HotCacheTableModel) for more details.
* **gRPC Proxies** – If your cluster is configured with gRPC Proxies, this tab displays information about the requests sent and received as well as successful and failed requests. A Graph of message rates and durations is also displayed. This tab will only show when connected via JMX and is not supported for REST connections. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#GrpcProxyTableModel) for more details.
* **Health** – If your cluster supports the Health Check API, this tab displays information regarding the status of all health endpoints. See [here](https://github.com/oracle/coherence-visualvm/blob/main/help/help.adoc#HealthSummaryTableModel) for more details.

## <a id="tracer"></a> Using Coherence with the Tracer framework

Version 1.7.0 of the Coherence VisualVM Plugin introduces initial integration with the VisualVM Tracer framework.
 
From the VisualVM website 

> The VisualVM Tracer framework provides detailed monitoring and analyzing Java applications. Using various probes, 
> the Tracer gathers metrics from an application and displays the data in a timeline. The data are 
> displayed both graphically and in a table and can be exported to common formats for further processing 
> by external tools.

When you connect to a cluster via JMX, you will see the `Tracer` tab as shown below:

&nbsp;&nbsp;  ![Coherence VisualVM Probes](assets/probes.png)

Each of the probes areas can be expanded to reveal the individual probes. You can select the probes and then 
click `Start` to display the information.

The supported Coherence probes are:

*Cluster Overview*

&nbsp; &nbsp; ![Cluster Overview](assets/probes-cluster-overview.png)

*Services*

&nbsp; &nbsp; ![Cluster Overview](assets/probes-services.png)

*Caches*

&nbsp; &nbsp; ![Caches](assets/probes-caches.png)

*Proxy Servers*

&nbsp; &nbsp; ![Proxy Servers](assets/probes-proxies.png)

*Persistence*

&nbsp; &nbsp; ![Persistence](assets/probes-persistence.png)

*Federation*

&nbsp; &nbsp; ![Federation](assets/probes-federation.png)

*Elastic Data*

&nbsp; &nbsp; ![elastic Data](assets/probes-elastic-data.png)
 
> Note: In the initial release of this integration, only summary information can to be plotted. We may include
> additional functionality in future releases to allow for specific services or caches to be monitored.
> There are no timelines for these releases. If you would like specific information included, please raise an issue.

## <a id="build"></a> Building the Plugin

If you wish to build the Plugin from scratch please follow the instructions below.

### Pre-requisites

You must have the following:

1. Java JDK 11+ - To build and test the plugin
2. Maven 3.6.3+
3. Git
 
Note: 
To install the tracer support, you must also run the script `./scrips/install-tracer-library.sh`

Ensure you set your environment to JDK 1.8 to run this script.

### Clone the Repository

1. Clone the Coherence VisualVM repository

   ```bash
   git clone https://github.com/oracle/coherence-visualvm.git
   ```

### Build the VisualVM Plugin

1. Ensure you have JDK11 or above in your PATH.

2. Build the Plugin

   From the `coherence-visualvm` directory:
   
   ```bash
   mvn clean install -DskipTests
   ```

   If you wish to run the Community Edition tests then leave out the `-DskipTests`.

3. Install the Plugin

   The plugin will be available in the location `coherence-visualvm-plugin/target/coherence-visualvm-plugin-{version}.nbm`
   
   Follow the instructions [here](https://docs.oracle.com/en/middleware/standalone/coherence/14.1.1.2206/manage/using-jmx-manage-oracle-coherence.html)
   to install the plugin manually.

## Contributing

This project is not accepting external contributions at this time. For bugs or enhancement requests, please file a GitHub issue unless it’s security related. When filing a bug remember that the better written the bug is, the more likely it is to be fixed. If you think you’ve found a security vulnerability, do not raise a GitHub issue and follow the instructions in our [security policy](./SECURITY.md).

## Security

Please consult the [security guide](./SECURITY.md) for our responsible security vulnerability disclosure process

## License

Released under the GNU General Public License (GPL)

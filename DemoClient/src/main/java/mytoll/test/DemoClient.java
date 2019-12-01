package mytoll.test;


import java.nio.ByteBuffer;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.sasl.Sasl;

import org.apache.hadoop.hbase.HBaseConfiguration;

import org.apache.hadoop.hbase.thrift2.generated.TColumnValue;
import org.apache.hadoop.hbase.thrift2.generated.TGet;
import org.apache.hadoop.hbase.thrift2.generated.THBaseService;
import org.apache.hadoop.hbase.thrift2.generated.TPut;
import org.apache.hadoop.hbase.thrift2.generated.TResult;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSaslClientTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class DemoClient {

  private static String host = "localhost";
  private static int port = 9091;
  private static boolean secure = false;
  private static String user = null;
  private static String kdc= null;
  private static String realm= null;
  private static String principal= null;
  private static String keytab= null;

  public static void main(String[] args) throws Exception {
    System.out.println("Thrift2 Demo");
    System.out.println("This demo assumes you have a table called \"example\" with a column family called \"family1\"");

    // use passed in arguments instead of defaults
    if (args.length != 4) {
	System.out.println("Usage: DemoClient [host=localhost] [port=9091] [kdc] [realm]");
	return; 
    }

    host = args[0];
    port = Integer.parseInt(args[1]);
    kdc = args[2];
    realm = args[3];

    org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
    conf.addResource("hbase-site.xml");
    String ker= conf.get("hbase.security.authentication");
    secure = true;
    user = "hbase";
    principal = "thrift2@"+realm;
    // this is the user name for the thrift2 server, by default, its "hbase" which is different user from thrift client
   

    final DemoClient client = new DemoClient();
    Subject.doAs(getSubject(),
      new PrivilegedExceptionAction<Void>() {
        @Override
        public Void run() throws Exception {
          client.run();
          return null;
        }
      });
  }

  public void run() throws Exception {
    int timeout = 10000;
    boolean framed = false;

    TTransport transport = new TSocket(host, port, timeout);
    if (framed) {
      transport = new TFramedTransport(transport);
    } else if (secure) {
      /**
       * The Thrift server the DemoClient is trying to connect to
       * must have a matching principal, and support authentication.
       *
       * The HBase cluster must be secure, allow proxy user.
       */
      Map<String, String> saslProperties = new HashMap<String, String>();
      saslProperties.put(Sasl.QOP, "auth-conf,auth-int,auth");
      transport = new TSaslClientTransport("GSSAPI", null,
        user != null ? user : "hbase",// Thrift server user name, should be an authorized proxy user
        host, // Thrift server domain
        saslProperties, null, transport);
    }

    TProtocol protocol = new TBinaryProtocol(transport);
    // This is our thrift client.
    THBaseService.Iface client = new THBaseService.Client(protocol);

    // open the transport
    transport.open();

    ByteBuffer table = ByteBuffer.wrap("example".getBytes());

    TPut put = new TPut();
    put.setRow("row1".getBytes());

    TColumnValue columnValue = new TColumnValue();
    columnValue.setFamily("family1".getBytes());
    columnValue.setQualifier("qualifier1".getBytes());
    columnValue.setValue("value1".getBytes());
    List<TColumnValue> columnValues = new ArrayList<TColumnValue>();
    columnValues.add(columnValue);
    put.setColumnValues(columnValues);

    client.put(table, put);

    TGet get = new TGet();
    get.setRow("row1".getBytes());

    TResult result = client.get(table, get);

    System.out.println("row = " + new String(result.getRow()));
    for (TColumnValue resultColumnValue : result.getColumnValues()) {
      System.out.println("family = " + new String(resultColumnValue.getFamily()));
      System.out.println("qualifier = " + new String(resultColumnValue.getFamily()));
      System.out.println("value = " + new String(resultColumnValue.getValue()));
      System.out.println("timestamp = " + resultColumnValue.getTimestamp());
    }

    transport.close();
  }

  static Subject getSubject() throws Exception {
    if (!secure) return new Subject();

    /*
     * To authenticate the DemoClient, kinit should be invoked ahead.
     * Here we try to get the Kerberos credential from the ticket cache.
     */
    // to address can not find the KDC error
    System.setProperty("java.security.krb5.kdc",kdc);
    System.setProperty("java.security.krb5.realm",realm);

    LoginContext context = new LoginContext("", new Subject(), null,
      new Configuration() {
        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
          Map<String, String> options = new HashMap<String, String>();
	  // update the useKyeTab ,keyTab and principal property
          options.put("useKeyTab", "true");
          options.put("keyTab", "thrift2.keytab");
          options.put("storeKey", "false");
          options.put("principal", principal);
          options.put("doNotPrompt", "true");
          options.put("useTicketCache", "true");
          options.put("renewTGT", "true");
          options.put("refreshKrb5Config", "true");
          options.put("isInitiator", "true");
          String ticketCache = System.getenv("KRB5CCNAME");
          if (ticketCache != null) {
            options.put("ticketCache", ticketCache);
          }
          options.put("debug", "true");

          return new AppConfigurationEntry[]{
              new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                  AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                  options)};
        }
      });
    context.login();
    return context.getSubject();
  }
}

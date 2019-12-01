hbase thrift2 java example reference: https://github.com/apache/hbase/blob/branch-1.4/hbase-examples/src/main/java/org/apache/hadoop/hbase/thrift/DemoClient.java

preprequest

#copy hbase-site.xml and user key tab to the class path

#run the thrift2:
sudo /usr/lib/hbase/bin/hbase-daemon.sh start thrift2 -p 9091 --infoport 9096

#add the clinet principal into the kdc
sudo kadmin.local
ank thrift2
xst -k thrift2.keytab -norandkey thrift2

#copy thrift2.keytab to the class path

compile and run:
javac -cp /usr/lib/hbase/*:/usr/lib/hbase/lib/* mytoll/test/DemoClient.java
java -cp .:/usr/lib/hbase/*:/usr/lib/hbase/lib/* mytoll.test.DemoClient ip-172-31-26-37.ap-southeast-2.compute.internal 9091 ip-172-31-26-37.ap-southeast-2.compute.internal test.com

hbase thrift2 java example reference: https://github.com/apache/hbase/blob/branch-1.4/hbase-examples/src/main/java/org/apache/hadoop/hbase/thrift/DemoClient.java

preprequest

#create hbase table:

create 'example', 'family1'


#run the thrift2:
sudo /usr/lib/hbase/bin/hbase-daemon.sh start thrift2 -p 9091 --infoport 9096

#add the clinet principal into the kdc
sudo kadmin.local
ank thrift2
xst -k thrift2.keytab -norandkey thrift2

#copy thrift2.keytab to the class path
Need thrift2.keytab in the class path. I tried to add the keytab into the jar however, its not working

example:

java -cp target/DemoClient-1.0-SNAPSHOT-jar-with-dependencies.jar mytoll.test.DemoClient ip-172-31-26-37.ap-southeast-2.compute.internal 9091 ip-172-31-26-37.ap-southeast-2.compute.internal test.com

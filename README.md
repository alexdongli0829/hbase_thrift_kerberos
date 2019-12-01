hbase thrift2 java example reference: https://github.com/apache/hbase/blob/branch-1.4/hbase-examples/src/main/java/org/apache/hadoop/hbase/thrift/DemoClient.java

preprequest
copy hbase-site.xml and user key tab to the class path
run the thrift2:
sudo /usr/lib/hbase/bin/hbase-daemon.sh start thrift2 -p 9091 --infoport 9096


compile and run:
javac -cp /usr/lib/hbase/*:/usr/lib/hbase/lib/* mytoll/test/DemoClient.java
java -cp .:/usr/lib/hbase/*:/usr/lib/hbase/lib/* mytoll.test.DemoClient ip-172-31-19-216 9091 true

set PATH=%PATH%;C:\eclipse\plugins\org.apache.ant_1.7.1.v20090120-1145\bin
set PATH=%PATH%;C:\Program Files (x86)\Java\jre6\bin
set JAVA_HOME=C:\Program Files (x86)\Java\jdk1.6.0_17
ant -f buildPerthis.xml
fc /l c:\scope\outputOrig.txt c:\scope\outputPerscope.txt




















































@rem mvn exec:java -f ./render/pom.xml  -Dlog4j.skipJansi=false -Dexec.mainClass=ikas.project.java.ms.App   -Dexec.args="-f ."

mvn compile && mvn exec:java  -Dlog4j.skipJansi=false -Dexec.mainClass=ikas.project.java.ms.App 


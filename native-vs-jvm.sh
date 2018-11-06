mvn clean
mvn package
$JAVA_HOME/bin/java -cp target/prettymd.jar com.pascal.prettymd.FormatPretty test-fail-native.md example.js  > jvm-out.log 2>&1
$JAVA_HOME/bin/native-image --language:js -cp target/prettymd.jar com.pascal.prettymd.FormatPretty prettymd
./prettymd test-fail-native.md example.js > native-out.log 2>&1
diff native-out.log jvm-out.log

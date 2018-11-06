package com.pascal.prettymd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import org.graalvm.polyglot.*;

public class FormatPretty {

    // INFO: default files are test.md and example.js (as arguments)
    public static void main(String[] args) throws IOException {
        String javaScript = readFromFile(args[1]);
        Context context = Context.create("js");
        context.eval("js", javaScript);
        Set<String> memberKeys = context.getBindings("js").getMemberKeys();
        JsonObject jsonObject = new JsonObject("TEST", "TEST");
        for (String memberKey : memberKeys) {
            Value v = context.getBindings("js").getMember(memberKey).execute(jsonObject);
            System.out.println(v.toString());
        }
    }

    public static String readFromFile(String fileName) throws IOException {
        FileReader fileReader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder sb = new StringBuilder();
        String currentLine;
        while ((currentLine = bufferedReader.readLine()) != null) {
            sb.append(currentLine + "\n");
        }
        bufferedReader.close();
        fileReader.close();
        return sb.toString();
    }
}

package com.pascal.prettymd;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FormatPretty {

    public static void main(String[] args) {
        Arguments arguments = new Arguments(args);
        Parser parser = Parser.builder().build();
        Node node = parser.parse(arguments.markdown);
        PrettyPrinterVisitor visitor = new PrettyPrinterVisitor(arguments.javaScript);
        node.accept(visitor);
        String result = visitor.getResult();
        System.out.print(result);
    }

    static class Arguments {
        final String markdown;
        final String javaScript;

        public Arguments(String[] args) {
            switch (args.length) {
                case 0:
                    throw new java.lang.IllegalArgumentException("ERROR: please provide at least a .md file as argument");
                case 1:
                    markdown = readFromFile(args[0]);
                    javaScript = "";
                    break;
                case 2:
                    markdown = readFromFile(args[0]);
                    javaScript = readFromFile(args[1]);
                    break;
                default:
                    throw new java.lang.IllegalArgumentException("ERROR: please provide maximal 2 arguments (.md file and .js file)");
            }
        }

        public static String readFromFile(String fileName) {
            try (FileReader fileReader = new FileReader(fileName);
                 BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                StringBuilder sb = new StringBuilder();
                String currentLine;
                while ((currentLine = bufferedReader.readLine()) != null) {
                    sb.append(currentLine).append("\n");
                }
                return sb.toString();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}

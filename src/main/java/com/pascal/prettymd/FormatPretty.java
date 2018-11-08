package com.pascal.prettymd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

public class FormatPretty {

	// INFO: default files are test.md and example.js (as arguments)

	public static void main(String[] args) throws IOException {

		String rawMarkdownText = "";
		String javaScript = "";

		switch (args.length) {
		case 0:
			throw new java.lang.RuntimeException("ERROR: please provide at least a .md file as argument");
		case 1:
			rawMarkdownText = readFromFile(args[0]);
			break;
		case 2:
			rawMarkdownText = readFromFile(args[0]);
			javaScript = readFromFile(args[1]);
			break;
		default:
			throw new java.lang.RuntimeException("ERROR: please provide maximal 2 arguments (.md file and .js file)");
		}

		Parser parser = Parser.builder().build();
		Node node = parser.parse(rawMarkdownText);
		PrettyPrinterVisitor visitor = new PrettyPrinterVisitor(javaScript);
		node.accept(visitor);
		String result = visitor.getResult();
		System.out.print(result);

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

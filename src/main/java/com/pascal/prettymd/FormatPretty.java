package com.pascal.prettymd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;

import org.graalvm.polyglot.*;
import org.graalvm.polyglot.proxy.*;

public class FormatPretty {
	
	// INFO: default files are test.md and example.js (as arguments)

	public static void main(String[] args) throws IOException {
		
		String rawMarkdownText = "";
		String javaScript = "";
		
		if(args.length == 0) {
			throw new java.lang.RuntimeException("ERROR: please provide at least a .md file as argument");
		} else if(args.length == 1){
			System.out.println("INFO: you can provide a JavaScript file as a second argument...\n");
			rawMarkdownText = readFromFile(args[0]);
		} else if(args.length ==2) {
			rawMarkdownText = readFromFile(args[0]);
			javaScript = readFromFile(args[1]);
		} else {
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
	
	// used for testing
	public static String testPrettyPrint(String testMarkdown) {
		Parser parser = Parser.builder().build();		
		Node node = parser.parse(testMarkdown);
		PrettyPrinterVisitor visitor = new PrettyPrinterVisitor("");
		node.accept(visitor);
		String result = visitor.getResult();
		return result;
	}

}

class PrettyPrinterVisitor extends AbstractVisitor {
	
	private String javaScript = "";
	
	private StringBuilder result = new StringBuilder();
	
	// TODO: use linewidth
	//int maxLineWidth = 200;
	
	// variables for keeping track of current status
    int currentIndentation = 0;
    Stack<String> currentMode = new Stack<String>();
    int currentNumberInList = 1;
    boolean firstBlockQuote = true;
    boolean firstListItem = true;
    		
    // default symbols
    char bulletSymbol = '-';
    char orderedListDelimiter = '.';
    char codeBlockSymbol = '~';
    
    // custom constructor with javascript
    public PrettyPrinterVisitor(String javaScript) {
    	this.javaScript = javaScript;
    }
    
    // passing a string to the javascript function to manipulate it
    public String passToJavaScript(String type, String text, String options) {
    	
    	Context context = Context.create("js");
    	Timer timer = new Timer(true);
    	final Value lambda;
    	final Value v;
    	
    	JsonObject jsonObject = new JsonObject(type, text);
    	jsonObject.type = type;
    	jsonObject.text = text;
    	jsonObject.options = options;
    	
    	timer.schedule(new TimerTask() {
    	    @Override
    	    public void run() {
    	        context.close(true);
    	    }
    	}, 1000);
    	
    	
    	try {
    	    lambda = context.eval("js", javaScript);
    	    if(!lambda.canExecute()) return text;
    	    v = lambda.execute(jsonObject);
    	    if(!v.isString()) return text;
    	    assert false;
    	    return v.asString();
    	} catch (PolyglotException e) {
    	    assert e.isCancelled();
    	    return text;
    	}
    	
    	
    }
    
    public String getResult() {
    	return result.toString();
    }
    
    @Override
    public void visit(Document document) {
    	visitChildren(document);
    }
    
    @Override
    public void visit(Heading heading) {
    	for(int i=0; i<heading.getLevel(); i++) {
    		result.append("#");
    	}
    	//Text text = (Text) heading.getFirstChild();
    	result.append(" ");
    	currentMode.push("heading");
    	visitChildren(heading);
    	currentMode.pop();
    	result.append("\n");
    	result.append("\n");
    }
    
    @Override
    public void visit(Text text) {
    	
    	if(!currentMode.empty() && currentMode.peek().equals("heading")) {
    		
    		String modifiedString = passToJavaScript("header", text.getLiteral(), "");
			result.append(modifiedString);
    		//result.append(text.getLiteral());
    		
    	} else {
    		
    		for(int i=0; i<currentIndentation; i++) {
        		result.append(" ");
        	}
        	
        	if (!currentMode.empty() && currentMode.peek().equals("blockQuote") && !firstBlockQuote) result.append("\n");
        	
        	if(!currentMode.empty() && currentMode.peek().equals("bulletList")) {
        		result.append(bulletSymbol + " ");
        	} else if (!currentMode.empty() && currentMode.peek().equals("orderedList")) {
        		result.append(currentNumberInList + "" + orderedListDelimiter + " ");
        		currentNumberInList++;
        	} else if (!currentMode.empty() && currentMode.peek().equals("blockQuote")) {
        		result.append("> ");
        		firstBlockQuote = false;
        	}
        	
        	result.append(text.getLiteral());
    	}
    	
        visitChildren(text);
    }
    
    @Override
	public void visit(HardLineBreak hardLineBreak) {
    	result.append("  \n");
    	visitChildren(hardLineBreak);
	}
    
    @Override
	public void visit(SoftLineBreak softLineBreak) {
    	result.append(" ");
    	visitChildren(softLineBreak);
	}
    
    @Override
	public void visit(Paragraph paragraph) {
    	
    	if (!currentMode.empty() && currentMode.peek().equals("blockQuote") && !firstBlockQuote) {
    		result.append(">");
    	}
    	
    	visitChildren(paragraph);
    	if(currentMode.isEmpty()) result.append("\n");
    	result.append("\n");
	}
    
    @Override
	public void visit(BulletList bulletList) {
    	if(!currentMode.isEmpty() && (currentMode.peek().equals("bulletList") || currentMode.peek().equals("orderedList"))) {
    		result.append("\n");
    	}
    	currentIndentation += 2;
    	currentMode.push("bulletList");
    	firstListItem = true;
    	visitChildren(bulletList);
    	firstListItem = false;
    	currentIndentation -= 2;
    	currentMode.pop();
    	result.append("\n");
	}
    
    @Override
	public void visit(OrderedList orderedList) {
    	if(!currentMode.isEmpty() && (currentMode.peek().equals("bulletList") || currentMode.peek().equals("orderedList"))) {
    		result.append("\n");
    	}
    	currentIndentation += 2;
    	currentMode.push("orderedList");
    	currentNumberInList = orderedList.getStartNumber();
    	orderedListDelimiter = orderedList.getDelimiter();
    	firstListItem = true;
    	visitChildren(orderedList);
    	firstListItem = false;
    	currentNumberInList = 1;
    	currentIndentation -= 2;
    	currentMode.pop();
    	result.append("\n");
	}
    
    @Override
	public void visit(Emphasis emphasis) {
    	result.append("*");
    	visitChildren(emphasis);
    	result.append("*");
	}
    
    @Override
	public void visit(StrongEmphasis strongEmphasis) {
    	result.append("**");
    	visitChildren(strongEmphasis);
    	result.append("**");
	}
    
    @Override
   	public void visit(ListItem listItem) {
   		visitChildren(listItem);
   	}

    @Override
	public void visit(Code code) {
    	result.append("`");
    	result.append(code.getLiteral());
    	visitChildren(code);
    	result.append("`");
	}
    
    @Override
    public void visit(BlockQuote blockQuote) {
    	currentMode.push("blockQuote");
    	firstBlockQuote = true;
    	visitChildren(blockQuote);
    	result.append("\n");
    	firstBlockQuote = false;
    	currentMode.pop();
	}
    
    @Override
	public void visit(IndentedCodeBlock indentedCodeBlock) {
    	//result.append("\n");
    	String codeBlockIndent = "    ";
    	for(int i=0; i<currentIndentation; i++) {
    		codeBlockIndent += " ";
    	}
    	StringBuilder codeBlock = new StringBuilder(indentedCodeBlock.getLiteral());
    	codeBlock.insert(0, codeBlockIndent);
    	for(int i=0; i<codeBlock.length(); i++) {
    		if(codeBlock.charAt(i) == '\n' && i!=codeBlock.length()-1) {
    			codeBlock.insert(i+1, codeBlockIndent);
    		}
    	}
    	result.append(codeBlock.toString());
    	visitChildren(indentedCodeBlock);
    	result.append("\n");
	}
    
    @Override
	public void visit(FencedCodeBlock fencedCodeBlock) {
    	codeBlockSymbol = fencedCodeBlock.getFenceChar();
    	result.append(codeBlockSymbol);
    	result.append(codeBlockSymbol);
    	result.append(codeBlockSymbol);
    	result.append(fencedCodeBlock.getInfo());
    	result.append("\n");
    	result.append(fencedCodeBlock.getLiteral());
    	visitChildren(fencedCodeBlock);
    	result.append(codeBlockSymbol);
    	result.append(codeBlockSymbol);
    	result.append(codeBlockSymbol);
    	result.append("\n");
    	result.append("\n");
	}
    
    @Override
	public void visit(Link link) {
    	result.append("[");
    	visitChildren(link);
    	result.append("]");
    	result.append("(" + link.getDestination() + ")");
	}
    
	@Override
	public void visit(ThematicBreak thematicBreak) {
		visitChildren(thematicBreak);
		result.append("***");
		result.append("\n");
		result.append("\n");
	}
	
	@Override
	public void visit(Image image) {
		result.append("![");
		visitChildren(image);
		result.append("]");
		result.append("(" + image.getDestination());
		result.append(" \"" + image.getTitle() + "\")");
	}
    
}

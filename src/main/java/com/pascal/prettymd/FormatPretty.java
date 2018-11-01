package com.pascal.prettymd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
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
	
	private Context context = Context.create("js");
	private Set<String> members;
	private StringBuilder result = new StringBuilder();
	
	// TODO: use line width
	//int maxLineWidth = 200;
	
	// variables for keeping track of current status
    int currentIndentation = 0;
    Stack<String> currentMode = new Stack<String>();
    int currentNumberInList = 1;
    int currentHeadingLevel = 0;
    boolean firstBlockQuote = true;
    boolean firstListItem = true;
    		
    // default symbols
    char bulletSymbol = '-';
    char orderedListDelimiter = '.';
    char codeBlockSymbol = '~';
    String emphasisSymbol = "*";
    String strongEmphasisSymbol = "**";
    char codeSymbol = '`';
    String headingSymbol = "#";
    
    // custom constructor with JavaScript
    public PrettyPrinterVisitor(String javaScript) {
    	this.context.eval("js", javaScript);
    	this.members = this.context.getBindings("js").getMemberKeys();
    }
    
    // passing a string to the JavaScript function to manipulate it
    public String passToJavaScript(String type, String text, String options) {
    	
    	Timer timer = new Timer(true);
    	final Value v;
    	JsonObject jsonObject = new JsonObject(text, options);
    	
    	timer.schedule(new TimerTask() {
    	    @Override
    	    public void run() {
    	        context.close(true);
    	    }
    	}, 5000);
    	
    	try {
    	    if(!members.contains(type)) return text;
    	    if(!context.getBindings("js").getMember(type).canExecute()) return text;
    	    v = context.getBindings("js").getMember(type).execute(jsonObject);
    	    if(!v.isString()) return text;
    	    assert false;
    	    return v.asString();
    	} catch (PolyglotException e) {
    	    assert e.isCancelled();
    	    return text;
    	}
    }
    
    public void printNewLines() {
    	if(currentMode.empty() || (!currentMode.empty() && (!currentMode.peek().equals("bulletList") &&
    			!currentMode.peek().equals("orderedList")))) {
    		String lines = passToJavaScript("processLineSpacing", "\n", "");
    		result.append(lines);
    	}
    }
    
    public String getResult() {
    	return result.toString();
    }
    
    /*
     * Visitor functions
     * 
     */
    
    @Override
    public void visit(Document document) {
    	visitChildren(document);
    }
    
    @Override
    public void visit(Heading heading) {
    	currentHeadingLevel = heading.getLevel();
    	currentMode.push("heading");
    	visitChildren(heading);
    	currentMode.pop();
    	currentHeadingLevel = 0;
    	result.append("\n");
    	printNewLines();
    }
    
    @Override
    public void visit(Text text) {
    	
    	// Heading
    	if(!currentMode.empty() && currentMode.peek().equals("heading")) {
    		StringBuilder sb = new StringBuilder();
    		for(int i=0; i<currentHeadingLevel; i++) {
        		sb.append(headingSymbol);
        	}
    		String modifiedString = passToJavaScript("processHeader", sb.toString() + " " + text.getLiteral(), "");
			result.append(modifiedString);
    	} else {
    		for(int i=0; i<currentIndentation; i++) {
        		result.append(" ");
        	}
    		// Spacing before beginning a block quote
        	if (!currentMode.empty() && currentMode.peek().equals("blockQuote") && !firstBlockQuote) {
        		result.append("\n");
        	}
        	// Bullet List
        	if(!currentMode.empty() && currentMode.peek().equals("bulletList")) {
        		String modifiedString = passToJavaScript("processBulletList", bulletSymbol + " " + text.getLiteral(), "");
    			result.append(modifiedString);
        	} 
        	// Ordered List
        	else if (!currentMode.empty() && currentMode.peek().equals("orderedList")) {
        		String modifiedString = passToJavaScript("processOrderedList", currentNumberInList++ + "" + orderedListDelimiter + " " + text.getLiteral(), "");
        		result.append(modifiedString);
        	} 
        	// Block Quotes
        	else if (!currentMode.empty() && currentMode.peek().equals("blockQuote")) {
        		firstBlockQuote = false;        		
        		String modifiedString = passToJavaScript("processBlockQuote", "> " + text.getLiteral(), "");
    			result.append(modifiedString);
        	} 
        	// Emphasis
        	else if(!currentMode.empty() && currentMode.peek().equals("emphasis")) {
        		String modifiedString = passToJavaScript("processEmphasis", text.getLiteral(), "");
    			result.append(modifiedString);
        	}
        	// Strong Emphasis
        	else if(!currentMode.empty() && currentMode.peek().equals("strongEmphasis")) {
        		String modifiedString = passToJavaScript("processStrongEmphasis", text.getLiteral(), "");
    			result.append(modifiedString);
        	}
        	// Link
        	else if(!currentMode.empty() && currentMode.peek().equals("link")) {
        		String modifiedString = passToJavaScript("processLinkText", text.getLiteral(), "");
    			result.append(modifiedString);
        	}
        	// Image
        	else if(!currentMode.empty() && currentMode.peek().equals("image")) {
        		String modifiedString = passToJavaScript("processImageText", text.getLiteral(), "");
    			result.append(modifiedString);
        	}
        	// Normal Text
        	else {
        		String modifiedString = passToJavaScript("processText", text.getLiteral(), "");
    			result.append(modifiedString);
        	}
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
    	if(!(!currentMode.empty() && currentMode.peek().equals("blockQuote"))) {
    		result.append(" ");
    	} 
    	visitChildren(softLineBreak);
	}
    
    @Override
	public void visit(Paragraph paragraph) {
    	if (!currentMode.empty() && currentMode.peek().equals("blockQuote") && !firstBlockQuote) {
    		result.append(">");
    	}
    	visitChildren(paragraph);
    	if(currentMode.isEmpty()) result.append("\n");
    	if(!currentMode.empty() && (currentMode.peek().equals("bulletList") || currentMode.peek().equals("orderedList"))) result.append("\n");
    	printNewLines();
	}
    
    @Override
	public void visit(BulletList bulletList) {
    	if(!currentMode.isEmpty() && (currentMode.peek().equals("bulletList") || currentMode.peek().equals("orderedList"))) {
    		result.append("\n");
    	}
    	currentIndentation += 2;
    	currentMode.push("bulletList");
    	bulletSymbol = bulletList.getBulletMarker();
    	firstListItem = true;
    	visitChildren(bulletList);
    	firstListItem = false;
    	currentIndentation -= 2;
    	currentMode.pop();
    	String lines = passToJavaScript("processLineSpacing", "\n", "");
		result.append(lines);
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
    	String lines = passToJavaScript("processLineSpacing", "\n", "");
		result.append(lines);
	}
    
    @Override
	public void visit(Emphasis emphasis) {
    	emphasisSymbol = emphasis.getOpeningDelimiter();
    	result.append(emphasisSymbol);
    	currentMode.push("emphasis");
    	visitChildren(emphasis);
    	currentMode.pop();
    	result.append(emphasisSymbol);
	}
    
    @Override
	public void visit(StrongEmphasis strongEmphasis) {
    	strongEmphasisSymbol = strongEmphasis.getOpeningDelimiter();
    	result.append(strongEmphasisSymbol);
    	currentMode.push("strongEmphasis");
    	visitChildren(strongEmphasis);
    	currentMode.pop();
    	result.append(strongEmphasisSymbol);
	}
    
    @Override
   	public void visit(ListItem listItem) {
   		visitChildren(listItem);
   	}

    @Override
	public void visit(Code code) {
    	//visitChildren(code); not working anymore  	
    	String modifiedString = passToJavaScript("processCode", "`" + code.getLiteral() + "`", "");
		result.append(modifiedString);
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
    	
    	String modifiedString = passToJavaScript("processIndentedCodeBlock", codeBlock.toString(), "");
		result.append(modifiedString);
    	visitChildren(indentedCodeBlock);
    	printNewLines();
	}
    
    @Override
	public void visit(FencedCodeBlock fencedCodeBlock) {
    	StringBuilder sb = new StringBuilder();    	
    	codeBlockSymbol = fencedCodeBlock.getFenceChar();
    	for(int i=0; i<fencedCodeBlock.getFenceLength(); i++) {
    		sb.append(codeBlockSymbol);
    	}
    	sb.append(fencedCodeBlock.getInfo());
    	sb.append("\n");
    	sb.append(fencedCodeBlock.getLiteral());
    	//visitChildren(fencedCodeBlock); not working anymore
    	for(int i=0; i<fencedCodeBlock.getFenceLength(); i++) {
    		sb.append(codeBlockSymbol);
    	}
    	sb.append("\n");
    	String modifiedString = passToJavaScript("processFencedCodeBlock", sb.toString(), "");
		result.append(modifiedString);
		printNewLines();
	}
    
    @Override
	public void visit(Link link) {
    	result.append("[");
    	currentMode.push("link");
    	visitChildren(link); // buggy: has a child for each word, should be just 1 child
    	currentMode.pop();
    	result.append("]");
    	String modifiedString = passToJavaScript("processLink", "(" + link.getDestination() + ")", "");
		result.append(modifiedString);
	}
    
	@Override
	public void visit(ThematicBreak thematicBreak) {
		visitChildren(thematicBreak);
		String modifiedString = passToJavaScript("processThematicBreak", "***\n", "");
		result.append(modifiedString);
		printNewLines();
	}
	
	@Override
	public void visit(Image image) {
		result.append("![");
		currentMode.push("image");
		visitChildren(image);
		currentMode.pop();
		result.append("]");
		String modifiedString = passToJavaScript("processImage", "(" + image.getDestination() + " \"" + image.getTitle() + "\")", "");
		result.append(modifiedString);
	}
    
}

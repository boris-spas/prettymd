package com.pascal.prettymd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;

public class FormatPretty {
	
	private static final String FILENAME = "test.md";
	
	static int maxLineWidth = 200;
	static String currentMode = "";

	public static void main(String[] args) throws IOException {
		
		String rawMarkdownText = "";
		
		/*if(args.length == 0) {
			System.out.println("please provide markdown file as argument");
			rawMarkdownText = readFromFile(FILENAME);
		} else {
			rawMarkdownText = readFromFile(args[0]);
		}*/
		
		rawMarkdownText = readFromFile(FILENAME);
		
		Parser parser = Parser.builder().build();		
		//Node node = parser.parse("Example\n=======\n\nSome more text");
		Node node = parser.parse(rawMarkdownText);
		WordCountVisitor visitor = new WordCountVisitor();		
		node.accept(visitor);
		
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

class WordCountVisitor extends AbstractVisitor {
	
    int lineBreaksAfterTitle = 2;
    int currentIndentation = 0;
    String currentMode = ""; // TODO: use a stack, otherwise nested things don't work
    char bulletSymbol = '*';
    
    @Override
    public void visit(Document document) {
    	visitChildren(document);
    }
    
    @Override
    public void visit(Heading heading) {
    	for(int i=0; i<heading.getLevel(); i++) {
    		System.out.print("#");
    	}
    	System.out.print(" ");
    	visitChildren(heading);
    }
    
    @Override
    public void visit(Text text) {
    	
    	for(int i=0; i<currentIndentation; i++) {
    		System.out.print(" ");
    	}
    	
    	if(currentMode.equals("bulletList")) {
    		System.out.print(bulletSymbol + " ");
    	} else if (currentMode.equals("orderedList")) {
    		// TODO: print right number
    		System.out.print("1. ");;
    	} else if (currentMode.equals("blockQuote")) {
    		System.out.print("> ");
    	}
    	
    	System.out.print(text.getLiteral());
    	
        visitChildren(text);
    }
    
    @Override
	public void visit(HardLineBreak hardLineBreak) {
		// TODO: Print new line, no hard break in file?
    	visitChildren(hardLineBreak);
	}
    
    @Override
	public void visit(SoftLineBreak softLineBreak) {
		// TODO: Print space, break if linewidth > as it should be
    	System.out.println();
    	visitChildren(softLineBreak);
	}
    
    @Override
	public void visit(Paragraph paragraph) {
    	System.out.println();
    	// TODO: First block quote should not print
    	if (currentMode.equals("blockQuote")) {
    		System.out.print("> ");
    	}
    	System.out.println();
    	visitChildren(paragraph);
	}
    
    @Override
	public void visit(BulletList bulletList) {
    	currentIndentation += 2;
    	currentMode = "bulletList";
    	visitChildren(bulletList);
    	currentIndentation -= 2;
    	currentMode = "";
	}
    
    @Override
	public void visit(OrderedList orderedList) {
    	currentIndentation = 2;
    	currentMode = "orderedList";
    	visitChildren(orderedList);
    	currentIndentation = 0;
    	currentMode = "";
	}
    
    @Override
	public void visit(Emphasis emphasis) {
    	System.out.print("*");
    	visitChildren(emphasis);
    	System.out.print("*");
	}
    
    @Override
	public void visit(StrongEmphasis strongEmphasis) {
    	System.out.print("**");
    	visitChildren(strongEmphasis);
    	System.out.print("**");
	}
    
    @Override
   	public void visit(ListItem listItem) {
   		visitChildren(listItem);
   	}

    @Override
	public void visit(Code code) {
    	System.out.print("`");
    	System.out.print(code.getLiteral());
    	visitChildren(code);
    	System.out.print("`");
	}
    
    @Override
    public void visit(BlockQuote blockQuote) {
    	currentMode = "blockQuote";
    	visitChildren(blockQuote);
    	currentMode = "";
	}
    
    @Override
	public void visit(IndentedCodeBlock indentedCodeBlock) {
    	System.out.println();
    	System.out.println();
    	StringBuilder codeBlock = new StringBuilder(indentedCodeBlock.getLiteral());
    	codeBlock.insert(0, "    ");
    	for(int i=0; i<codeBlock.length(); i++) {
    		if(codeBlock.charAt(i) == '\n' && i!=codeBlock.length()-1) {
    			codeBlock.insert(i+1, "    ");
    		}
    	}
    	System.out.print(codeBlock.toString());
    	visitChildren(indentedCodeBlock);
	}
    
    @Override
	public void visit(FencedCodeBlock fencedCodeBlock) {
    	System.out.println();
    	System.out.println();
    	System.out.print("```");
    	System.out.print(fencedCodeBlock.getInfo());
    	System.out.println();
    	System.out.print(fencedCodeBlock.getLiteral());
    	visitChildren(fencedCodeBlock);
    	System.out.println("```");
	}
    
    @Override
	public void visit(Link link) {
    	System.out.print("[");
    	visitChildren(link);
    	System.out.print("]");
    	System.out.print("(" + link.getDestination() + ")");
	}
    
	@Override
	public void visit(ThematicBreak thematicBreak) {
		System.out.println("");
		visitChildren(thematicBreak);
		System.out.println("");
		System.out.print("***");
	}
	
	@Override
	public void visit(Image image) {
		System.out.print("![");
		visitChildren(image);
		System.out.print("]");
		System.out.print("(" + image.getDestination());
		System.out.print(" \"" + image.getTitle() + "\")");
	}
	
	/*@Override
	public void visit(HtmlInline htmlInline) {
		// TODO Auto-generated method stub
	}

    @Override
	public void visit(HtmlBlock htmlBlock) {
		// TODO Auto-generated method stub
	}

    @Override
	public void visit(CustomBlock customBlock) {
		// TODO Auto-generated method stub
	}

    @Override
	public void visit(CustomNode customNode) {
		// TODO Auto-generated method stub
	}*/
    
}

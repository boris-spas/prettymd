package com.pascal.prettymd;

import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

public class PrettyPrinterVisitor extends AbstractVisitor {

	private Context context = Context.create("js");
	private Set<String> members;
	private StringBuilder result = new StringBuilder();

	// JavaScript function names
	private static final String SET_DEFAULTS = "setDefaults";
	private static final String PROCESS_HEADER = "processHeader";
	private static final String PROCESS_LINE_SPACING = "processLineSpacing";
	private static final String PROCESS_TEXT = "processText";
	private static final String PROCESS_BLOCK_QUOTE = "processBlockQuote";
	private static final String PROCESS_ORDERED_LIST = "processOrderedList";
	private static final String PROCESS_BULLET_LIST = "processBulletList";
	private static final String PROCESS_EMPHASIS = "processEmphasis";
	private static final String PROCESS_STRONG_EMPHASIS = "processStrongEmphasis";
	private static final String PROCESS_CODE = "processCode";
	private static final String PROCESS_INDENTED_CODE_BLOCK = "processIndentedCodeBlock";
	private static final String PROCESS_FENCED_CODE_BLOCK = "processFencedCodeBlock";
	private static final String PROCESS_THEMATIC_BREAK = "processThematicBreak";
	private static final String PROCESS_LINK_TEXT = "processLinkText";
	private static final String PROCESS_LINK = "processLink";
	private static final String PROCESS_IMAGE_TEXT = "processImageText";
	private static final String PROCESS_IMAGE = "processImage";

	// possible modes
	private static final String BULLET_LIST = "bulletList";
	private static final String ORDERED_LIST = "orderedList";
	private static final String HEADING = "heading";
	private static final String BLOCK_QUOTE = "blockQuote";
	private static final String EMPHASIS = "emphasis";
	private static final String STRONG_EMPHASIS = "strongEmphasis";
	private static final String LINK = "link";
	private static final String IMAGE = "image";

	// variables for keeping track of current status
	private int currentIndentation = 0;
	private Stack<String> currentMode = new Stack<String>();
	private int currentNumberInList = 1;
	private int currentHeadingLevel = 0;
	private boolean firstBlockQuote = true;
	private int currentLineWidth = 0; // TODO: currently not used

	// default symbols/settings
	private int maxLineWidth = 200; // TODO: currently not used
	private char bulletSymbol = '-';
	private char orderedListDelimiter = '.';
	private char codeBlockSymbol = '~';
	private String emphasisSymbol = "*";
	private String strongEmphasisSymbol = "**";
	private char codeSymbol = '`';
	private String headingSymbol = "#";

	// custom constructor with JavaScript
	public PrettyPrinterVisitor(String javaScript) {
		this.context.eval("js", javaScript);
		this.members = this.context.getBindings("js").getMemberKeys();
		// TODO: not working as desired
		// context.getBindings("js").getMember(SET_DEFAULTS).execute(maxLineWidth);
	}

	// passing a string to the JavaScript function to manipulate it
	public String passToJavaScript(String type, String text, String options) {

		Timer timer = new Timer(true);
		final Value v;

		try {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					context.close(true);
				}
			}, 5000);
			if (!members.contains(type))
				return text;
			if (!context.getBindings("js").getMember(type).canExecute())
				return text;
			v = context.getBindings("js").getMember(type).execute(text);
			if (!v.isString())
				return text;
			assert false;
			return v.asString();
		} catch (PolyglotException e) {
			assert e.isCancelled();
			return text;
		} finally {
			timer.cancel();
		}
	}

	public void printNewLines() {
		if (currentMode.empty() || (!currentMode.empty()
				&& (!currentMode.peek().equals(BULLET_LIST) && !currentMode.peek().equals(ORDERED_LIST)))) {
			String lines = passToJavaScript(PROCESS_LINE_SPACING, "\n", "");
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
		currentMode.push(HEADING);
		visitChildren(heading);
		currentMode.pop();
		currentHeadingLevel = 0;
		result.append("\n");
		printNewLines();
	}

	@Override
	public void visit(Text text) {

		// Heading
		if (!currentMode.empty() && currentMode.peek().equals(HEADING)) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < currentHeadingLevel; i++) {
				sb.append(headingSymbol);
			}
			String modifiedString = passToJavaScript(PROCESS_HEADER, sb.toString() + " " + text.getLiteral(), "");
			result.append(modifiedString);
		} else {
			// TODO: switch case
			for (int i = 0; i < currentIndentation; i++) {
				result.append(" ");
			}
			// Spacing before beginning a block quote
			if (!currentMode.empty() && currentMode.peek().equals(BLOCK_QUOTE) && !firstBlockQuote) {
				result.append("\n");
			}
			// Bullet List
			if (!currentMode.empty() && currentMode.peek().equals(BULLET_LIST)) {
				String modifiedString = passToJavaScript(PROCESS_BULLET_LIST, bulletSymbol + " " + text.getLiteral(),
						"");
				result.append(modifiedString);
			}
			// Ordered List
			else if (!currentMode.empty() && currentMode.peek().equals(ORDERED_LIST)) {
				String modifiedString = passToJavaScript(PROCESS_ORDERED_LIST,
						currentNumberInList++ + "" + orderedListDelimiter + " " + text.getLiteral(), "");
				result.append(modifiedString);
			}
			// Block Quotes
			else if (!currentMode.empty() && currentMode.peek().equals(BLOCK_QUOTE)) {
				firstBlockQuote = false;
				String modifiedString = passToJavaScript(PROCESS_BLOCK_QUOTE, "> " + text.getLiteral(), "");
				result.append(modifiedString);
			}
			// Emphasis
			else if (!currentMode.empty() && currentMode.peek().equals(EMPHASIS)) {
				String modifiedString = passToJavaScript(PROCESS_EMPHASIS, text.getLiteral(), "");
				result.append(modifiedString);
			}
			// Strong Emphasis
			else if (!currentMode.empty() && currentMode.peek().equals(STRONG_EMPHASIS)) {
				String modifiedString = passToJavaScript(PROCESS_STRONG_EMPHASIS, text.getLiteral(), "");
				result.append(modifiedString);
			}
			// Link
			else if (!currentMode.empty() && currentMode.peek().equals(LINK)) {
				String modifiedString = passToJavaScript(PROCESS_LINK_TEXT, text.getLiteral(), "");
				result.append(modifiedString);
			}
			// Image
			else if (!currentMode.empty() && currentMode.peek().equals(IMAGE)) {
				String modifiedString = passToJavaScript(PROCESS_IMAGE_TEXT, text.getLiteral(), "");
				result.append(modifiedString);
			}
			// Normal Text
			else {
				String modifiedString = passToJavaScript(PROCESS_TEXT, text.getLiteral(), "");
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
		if (!(!currentMode.empty() && currentMode.peek().equals(BLOCK_QUOTE))) {
			result.append(" ");
		}
		visitChildren(softLineBreak);
	}

	@Override
	public void visit(Paragraph paragraph) {
		if (!currentMode.empty() && currentMode.peek().equals(BLOCK_QUOTE) && !firstBlockQuote) {
			result.append(">");
		}
		visitChildren(paragraph);
		if (currentMode.isEmpty())
			result.append("\n");
		if (!currentMode.empty() && (currentMode.peek().equals(BULLET_LIST) || currentMode.peek().equals(ORDERED_LIST)))
			result.append("\n");
		printNewLines();
	}

	@Override
	public void visit(BulletList bulletList) {
		if (!currentMode.isEmpty()
				&& (currentMode.peek().equals(BULLET_LIST) || currentMode.peek().equals(ORDERED_LIST))) {
			result.append("\n");
		}
		currentIndentation += 2;
		currentMode.push(BULLET_LIST);
		bulletSymbol = bulletList.getBulletMarker();
		visitChildren(bulletList);
		currentIndentation -= 2;
		currentMode.pop();
		String lines = passToJavaScript(PROCESS_LINE_SPACING, "\n", "");
		result.append(lines);
	}

	@Override
	public void visit(OrderedList orderedList) {
		if (!currentMode.isEmpty()
				&& (currentMode.peek().equals(BULLET_LIST) || currentMode.peek().equals(ORDERED_LIST))) {
			result.append("\n");
		}
		currentIndentation += 2;
		currentMode.push(ORDERED_LIST);
		currentNumberInList = orderedList.getStartNumber();
		orderedListDelimiter = orderedList.getDelimiter();
		visitChildren(orderedList);
		currentNumberInList = 1;
		currentIndentation -= 2;
		currentMode.pop();
		String lines = passToJavaScript(PROCESS_LINE_SPACING, "\n", "");
		result.append(lines);
	}

	@Override
	public void visit(Emphasis emphasis) {
		emphasisSymbol = emphasis.getOpeningDelimiter();
		result.append(emphasisSymbol);
		currentMode.push(EMPHASIS);
		visitChildren(emphasis);
		currentMode.pop();
		result.append(emphasisSymbol);
	}

	@Override
	public void visit(StrongEmphasis strongEmphasis) {
		strongEmphasisSymbol = strongEmphasis.getOpeningDelimiter();
		result.append(strongEmphasisSymbol);
		currentMode.push(STRONG_EMPHASIS);
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
		// visitChildren(code); no longer working with current setup
		String modifiedString = passToJavaScript(PROCESS_CODE, codeSymbol + code.getLiteral() + codeSymbol, "");
		result.append(modifiedString);
	}

	@Override
	public void visit(BlockQuote blockQuote) {
		currentMode.push(BLOCK_QUOTE);
		firstBlockQuote = true;
		visitChildren(blockQuote);
		result.append("\n");
		firstBlockQuote = false;
		currentMode.pop();
	}

	@Override
	public void visit(IndentedCodeBlock indentedCodeBlock) {
		String codeBlockIndent = "    ";
		for (int i = 0; i < currentIndentation; i++) {
			codeBlockIndent += " ";
		}
		StringBuilder codeBlock = new StringBuilder(indentedCodeBlock.getLiteral());
		codeBlock.insert(0, codeBlockIndent);
		for (int i = 0; i < codeBlock.length(); i++) {
			if (codeBlock.charAt(i) == '\n' && i != codeBlock.length() - 1) {
				codeBlock.insert(i + 1, codeBlockIndent);
			}
		}

		String modifiedString = passToJavaScript(PROCESS_INDENTED_CODE_BLOCK, codeBlock.toString(), "");
		result.append(modifiedString);
		visitChildren(indentedCodeBlock);
		printNewLines();
	}

	@Override
	public void visit(FencedCodeBlock fencedCodeBlock) {
		StringBuilder sb = new StringBuilder();
		codeBlockSymbol = fencedCodeBlock.getFenceChar();
		for (int i = 0; i < fencedCodeBlock.getFenceLength(); i++) {
			sb.append(codeBlockSymbol);
		}
		sb.append(fencedCodeBlock.getInfo());
		sb.append("\n");
		sb.append(fencedCodeBlock.getLiteral());
		// visitChildren(fencedCodeBlock); no longer working with current setup
		for (int i = 0; i < fencedCodeBlock.getFenceLength(); i++) {
			sb.append(codeBlockSymbol);
		}
		sb.append("\n");
		String modifiedString = passToJavaScript(PROCESS_FENCED_CODE_BLOCK, sb.toString(), "");
		result.append(modifiedString);
		printNewLines();
	}

	@Override
	public void visit(Link link) {
		result.append("[");
		currentMode.push(LINK);
		visitChildren(link); // buggy: has a child for each word, should be just 1 child
		currentMode.pop();
		result.append("]");
		String modifiedString = passToJavaScript(PROCESS_LINK, "(" + link.getDestination() + ")", "");
		result.append(modifiedString);
	}

	@Override
	public void visit(ThematicBreak thematicBreak) {
		visitChildren(thematicBreak);
		String modifiedString = passToJavaScript(PROCESS_THEMATIC_BREAK, "***\n", "");
		result.append(modifiedString);
		printNewLines();
	}

	@Override
	public void visit(Image image) {
		result.append("![");
		currentMode.push(IMAGE);
		visitChildren(image);
		currentMode.pop();
		result.append("]");
		String modifiedString = passToJavaScript(PROCESS_IMAGE,
				"(" + image.getDestination() + " \"" + image.getTitle() + "\")", "");
		result.append(modifiedString);
	}

}

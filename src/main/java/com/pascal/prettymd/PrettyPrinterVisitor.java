package com.pascal.prettymd;

import org.commonmark.node.*;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.ArrayDeque;
import java.util.Deque;

import static com.pascal.prettymd.Constants.*;

public class PrettyPrinterVisitor extends AbstractVisitor {

    private final Context context = Context.create("js");
    private final StringBuilder result = new StringBuilder();

    MyProxyObject proxyObj = new MyProxyObject();

    // variables for keeping track of current status
    private int currentIndentation = 0;
    private final Deque<String> currentMode = new ArrayDeque<>();
    private int currentNumberInList = 1;
    private int currentHeadingLevel = 0;
    private boolean firstBlockQuote = true;
    private int currentLineWidth = 0;
    private String lastElement = "";

    // custom constructor with JavaScript
    public PrettyPrinterVisitor(String javaScript) {
        this.context.eval("js", javaScript);
        this.context.enter();
        setDefaultSymbols();
        if (context.getBindings("js").hasMember(SET_DEFAULTS)) {
            context.getBindings("js").getMember(SET_DEFAULTS).executeVoid(proxyObj);
        }
    }

    // passing a string to the JavaScript function to manipulate it
    public String passToJavaScript(String type, String text) {
        Value jsFunction = context.getBindings("js").getMember(type);
        if (jsFunction == null || !jsFunction.canExecute()) {
            return text;
        }
        final Value v = jsFunction.execute(text);
        if (!v.isString()) {
            return text;
        }
        return v.asString();
    }


    // default symbols/settings
    public void setDefaultSymbols() {
        proxyObj.putMember(MAX_LINE_WIDTH, Value.asValue(100));
        proxyObj.putMember(BULLET_SYMBOL, Value.asValue('-'));
        proxyObj.putMember(ORDERED_LIST_DELIMITER, Value.asValue('.'));
        proxyObj.putMember(CODE_BLOCK_SYMBOL, Value.asValue('~'));
        proxyObj.putMember(EMPHASIS_SYMBOL, Value.asValue("*"));
        proxyObj.putMember(STRONG_EMPHASIS_SYMBOL, Value.asValue("**"));
        proxyObj.putMember(CODE_SYMBOL, Value.asValue('`'));
        proxyObj.putMember(HEADING_SYMBOL, Value.asValue("#"));
        proxyObj.putMember(BLOCK_QUOTE_SYMBOL, Value.asValue(">"));
    }

    public void printNewLines() {
        if (currentMode.isEmpty() || (!currentMode.peek().equals(BULLET_LIST) && !currentMode.peek().equals(ORDERED_LIST))) {
            String lines = passToJavaScript(PROCESS_LINE_SPACING, "\n");
            result.append(lines);
            currentLineWidth = 0;
        }
    }

    public void appendToResult(String str) {
        if ((currentLineWidth + str.length()) > proxyObj.getMemberAsInt(MAX_LINE_WIDTH)) {

            while (!str.equals("")) {

                int iterationCounter = 1;

                // is string too long
                if ((currentLineWidth + str.length()) < proxyObj.getMemberAsInt(MAX_LINE_WIDTH)) {
                    for (int i = 0; i < currentIndentation; i++) {
                        result.append(" ");
                    }
                    result.append(str);
                    currentLineWidth += str.length();
                    break;
                }

                int offset = 0;
                int index = 0;
                if ((currentLineWidth + str.length()) < proxyObj.getMemberAsInt(MAX_LINE_WIDTH)) {
                    index = str.length() - 1;
                } else {
                    index = proxyObj.getMemberAsInt(MAX_LINE_WIDTH) - 1 - currentLineWidth;
                }

                // where to make the new line
                for (int i = index; i >= 0; i--) {
                    char c = str.charAt(i);
                    if (c == ' ') {
                        break;
                    } else {
                        offset++;
                    }
                }

                String line = str.substring(0, index - offset + 1);
                // indentation
                for (int i = 0; i < currentIndentation; i++) {
                    if (iterationCounter != 1) {
                        result.append(" ");
                    }
                }
                result.append(line);
                result.append("\n");
                currentLineWidth = 0;
                str = str.substring(index - offset + 1, str.length());

                iterationCounter++;
            }
        } else {
            result.append(str);
            currentLineWidth += str.length();
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
        currentLineWidth = 0;
        printNewLines();
    }

    @Override
    public void visit(Text text) {

        // Indentation
        String indentation = "";
        if (currentLineWidth == 0) {
            for (int i = 0; i < currentIndentation; i++) {
                indentation += " ";
            }
        }
        appendToResult(indentation);

        if (!currentMode.isEmpty()) {

            String mode = currentMode.peek();
            String modifiedString = "";

            switch (mode) {
                case HEADING:
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < currentHeadingLevel; i++) {
                        sb.append(proxyObj.getMember(HEADING_SYMBOL));
                    }
                    modifiedString = passToJavaScript(PROCESS_HEADER, sb + " " + text.getLiteral());
                    appendToResult(modifiedString);
                    break;
                case BULLET_LIST:
                    if (!lastElement.equals(BULLET_ITEM) && !lastElement.equals(NUMBERED_ITEM) && !lastElement.equals(TEXT)
                            && !lastElement.equals(EMPTY) && !lastElement.equals(ORDERED_LIST)
                            && !lastElement.equals(BULLET_LIST)) {
                        modifiedString = passToJavaScript(PROCESS_BULLET_LIST, text.getLiteral());
                    } else {
                        modifiedString = passToJavaScript(PROCESS_BULLET_LIST,
                                proxyObj.getMember(BULLET_SYMBOL) + " " + text.getLiteral());
                    }
                    appendToResult(modifiedString);
                    lastElement = BULLET_ITEM;
                    break;
                case ORDERED_LIST:
                    if (!lastElement.equals(BULLET_ITEM) && !lastElement.equals(NUMBERED_ITEM) && !lastElement.equals(TEXT)
                            && !lastElement.equals(EMPTY) && !lastElement.equals(ORDERED_LIST)
                            && !lastElement.equals(BULLET_LIST)) {
                        modifiedString = passToJavaScript(PROCESS_ORDERED_LIST, text.getLiteral());
                    } else {
                        modifiedString = passToJavaScript(PROCESS_ORDERED_LIST, currentNumberInList++ + ""
                                + proxyObj.getMember(ORDERED_LIST_DELIMITER) + " " + text.getLiteral());
                    }
                    appendToResult(modifiedString);
                    lastElement = NUMBERED_ITEM;
                    break;
                case BLOCK_QUOTE:
                    if (!firstBlockQuote) {
                        result.append("\n");
                        currentLineWidth = 0;
                    }
                    firstBlockQuote = false;
                    modifiedString = passToJavaScript(PROCESS_BLOCK_QUOTE,
                            proxyObj.getMember(BLOCK_QUOTE_SYMBOL) + " " + text.getLiteral());
                    appendToResult(modifiedString);
                    break;
                case EMPHASIS:
                    modifiedString = passToJavaScript(PROCESS_EMPHASIS, text.getLiteral());
                    appendToResult(modifiedString);
                    break;
                case STRONG_EMPHASIS:
                    modifiedString = passToJavaScript(PROCESS_STRONG_EMPHASIS, text.getLiteral());
                    appendToResult(modifiedString);
                    break;
                case LINK:
                    modifiedString = passToJavaScript(PROCESS_LINK_TEXT, text.getLiteral());
                    appendToResult(modifiedString);
                    break;
                case IMAGE:
                    modifiedString = passToJavaScript(PROCESS_IMAGE_TEXT, text.getLiteral());
                    appendToResult(modifiedString);
                    break;
            }
        } else {
            String modifiedString = passToJavaScript(PROCESS_TEXT, text.getLiteral());
            appendToResult(modifiedString);
            lastElement = TEXT;
        }
        visitChildren(text);
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        result.append("  \n");
        currentLineWidth = 0;
        visitChildren(hardLineBreak);
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        if (!(!currentMode.isEmpty() && currentMode.peek().equals(BLOCK_QUOTE))) {
            appendToResult(" ");
        }
        visitChildren(softLineBreak);
    }

    @Override
    public void visit(Paragraph paragraph) {
        if (!currentMode.isEmpty() && currentMode.peek().equals(BLOCK_QUOTE) && !firstBlockQuote) {
            appendToResult(proxyObj.getMember(BLOCK_QUOTE_SYMBOL).toString());
        }
        visitChildren(paragraph);
        if (currentMode.isEmpty()) {
            result.append("\n");
            currentLineWidth = 0;
        }
        if (!currentMode.isEmpty()
                && (currentMode.peek().equals(BULLET_LIST) || currentMode.peek().equals(ORDERED_LIST))) {
            result.append("\n");
            currentLineWidth = 0;
        }
        printNewLines();
    }

    @Override
    public void visit(BulletList bulletList) {
        if (!currentMode.isEmpty()
                && (currentMode.peek().equals(BULLET_LIST) || currentMode.peek().equals(ORDERED_LIST))) {
            result.append("\n");
            currentLineWidth = 0;
        }
        currentIndentation += 2;
        currentMode.push(BULLET_LIST);
        visitChildren(bulletList);
        currentIndentation -= 2;
        currentMode.pop();
        String lines = passToJavaScript(PROCESS_LINE_SPACING, "\n");
        result.append(lines);
        currentLineWidth = 0;
        lastElement = BULLET_LIST;
    }

    @Override
    public void visit(OrderedList orderedList) {
        if (!currentMode.isEmpty()
                && (currentMode.peek().equals(BULLET_LIST) || currentMode.peek().equals(ORDERED_LIST))) {
            result.append("\n");
            currentLineWidth = 0;
        }
        currentIndentation += 2;
        currentMode.push(ORDERED_LIST);
        currentNumberInList = orderedList.getStartNumber();
        visitChildren(orderedList);
        currentNumberInList = 1;
        currentIndentation -= 2;
        currentMode.pop();
        String lines = passToJavaScript(PROCESS_LINE_SPACING, "\n");
        result.append(lines);
        currentLineWidth = 0;
        lastElement = ORDERED_LIST;
    }

    @Override
    public void visit(Emphasis emphasis) {
        appendToResult(proxyObj.getMember(EMPHASIS_SYMBOL).toString());
        currentMode.push(EMPHASIS);
        visitChildren(emphasis);
        currentMode.pop();
        appendToResult(proxyObj.getMember(EMPHASIS_SYMBOL).toString());
        lastElement = EMPHASIS;
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        appendToResult(proxyObj.getMember(STRONG_EMPHASIS_SYMBOL).toString());
        currentMode.push(STRONG_EMPHASIS);
        visitChildren(strongEmphasis);
        currentMode.pop();
        appendToResult(proxyObj.getMember(STRONG_EMPHASIS_SYMBOL).toString());
        lastElement = STRONG_EMPHASIS;
    }

    @Override
    public void visit(ListItem listItem) {
        visitChildren(listItem);
    }

    @Override
    public void visit(Code code) {
        String modifiedString = passToJavaScript(PROCESS_CODE,
                proxyObj.getMember(CODE_SYMBOL) + code.getLiteral() + proxyObj.getMember(CODE_SYMBOL));
        appendToResult(modifiedString);
        lastElement = CODE;
    }

    @Override
    public void visit(BlockQuote blockQuote) {
        currentMode.push(BLOCK_QUOTE);
        firstBlockQuote = true;
        visitChildren(blockQuote);
        result.append("\n");
        currentLineWidth = 0;
        firstBlockQuote = false;
        currentMode.pop();
        lastElement = BLOCK_QUOTE;
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

        String modifiedString = passToJavaScript(PROCESS_INDENTED_CODE_BLOCK, codeBlock.toString());
        result.append(modifiedString);
        visitChildren(indentedCodeBlock);
        printNewLines();
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fencedCodeBlock.getFenceLength(); i++) {
            sb.append(proxyObj.getMember(CODE_BLOCK_SYMBOL));
        }
        sb.append(fencedCodeBlock.getInfo());
        sb.append("\n");
        sb.append(fencedCodeBlock.getLiteral());
        for (int i = 0; i < fencedCodeBlock.getFenceLength(); i++) {
            sb.append(proxyObj.getMember(CODE_BLOCK_SYMBOL));
        }
        sb.append("\n");
        String modifiedString = passToJavaScript(PROCESS_FENCED_CODE_BLOCK, sb.toString());
        result.append(modifiedString);
        printNewLines();
    }

    @Override
    public void visit(Link link) {
        appendToResult("[");
        currentMode.push(LINK);
        visitChildren(link);
        currentMode.pop();
        appendToResult("]");
        String modifiedString = passToJavaScript(PROCESS_LINK, "(" + link.getDestination() + ")");
        appendToResult(modifiedString);
        lastElement = LINK;
    }

    @Override
    public void visit(ThematicBreak thematicBreak) {
        visitChildren(thematicBreak);
        String modifiedString = passToJavaScript(PROCESS_THEMATIC_BREAK, "***\n");
        appendToResult(modifiedString);
        printNewLines();
    }

    @Override
    public void visit(Image image) {
        appendToResult("![");
        currentMode.push(IMAGE);
        visitChildren(image);
        currentMode.pop();
        appendToResult("]");
        String modifiedString = passToJavaScript(PROCESS_IMAGE,
                "(" + image.getDestination() + " \"" + image.getTitle() + "\")");
        appendToResult(modifiedString);
        lastElement = IMAGE;
    }

}

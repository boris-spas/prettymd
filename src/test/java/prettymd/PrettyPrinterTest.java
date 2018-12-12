package prettymd;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.junit.Test;

import com.pascal.prettymd.PrettyPrinterVisitor;

public class PrettyPrinterTest {

	private static final String RESOURCE_PATH = "src/test/resources/";
	private static final String SAMPLE = ".sample";
	private static final String DESIRED = ".desired";

	private Parser parser = Parser.builder().build();
	private PrettyPrinterVisitor visitor = new PrettyPrinterVisitor("");

	public static String loadFile(String filename) throws IOException {
		List<String> list = new LinkedList<String>();
		list = Files.readAllLines(Paths.get(RESOURCE_PATH + filename), StandardCharsets.UTF_8);
		return String.join("\n", list) + "\n";
	}

	public String testPrettyPrint(String testMarkdown) {
		Node node = parser.parse(testMarkdown);
		node.accept(visitor);
		return visitor.getResult();
	}

	@Test
	public void headerTest() throws IOException {
		String filename = "atx";
		String sample = loadFile(filename + SAMPLE);
		String desired = loadFile(filename + DESIRED);
		assertEquals(desired, testPrettyPrint(sample));
	}

	@Test
	public void blockTest() throws IOException {
		String filename = "block";
		String sample = loadFile(filename + SAMPLE);
		String desired = loadFile(filename + DESIRED);
		assertEquals(desired, testPrettyPrint(sample));
	}

	@Test
	public void horizontalRowTest() throws IOException {
		String filename = "hr";
		String sample = loadFile(filename + SAMPLE);
		String desired = loadFile(filename + DESIRED);
		assertEquals(desired, testPrettyPrint(sample));
	}

	@Test
	public void lineBreakTest() throws IOException {
		String filename = "line-break";
		String sample = loadFile(filename + SAMPLE);
		String desired = loadFile(filename + DESIRED);
		assertEquals(desired, testPrettyPrint(sample));
	}

	@Test
	public void mixedTest() throws IOException {
		String filename = "mixed";
		String sample = loadFile(filename + SAMPLE);
		String desired = loadFile(filename + DESIRED);
		assertEquals(desired, testPrettyPrint(sample));
	}

	@Test
	public void orderedListTest() throws IOException {
		String filename = "ol";
		String sample = loadFile(filename + SAMPLE);
		String desired = loadFile(filename + DESIRED);
		assertEquals(desired, testPrettyPrint(sample));
	}

	@Test
	public void orderedInUnorderdListTest() throws IOException {
		String filename = "ol-in-ul";
		String sample = loadFile(filename + SAMPLE);
		String desired = loadFile(filename + DESIRED);
		assertEquals(desired, testPrettyPrint(sample));
	}

	@Test
	public void paragraphTest() throws IOException {
		String filename = "p";
		String sample = loadFile(filename + SAMPLE);
		String desired = loadFile(filename + DESIRED);
		assertEquals(desired, testPrettyPrint(sample));
	}

	@Test
	public void alternateHeaderTest() throws IOException {
		String filename = "setext";
		String sample = loadFile(filename + SAMPLE);
		String desired = loadFile(filename + DESIRED);
		assertEquals(desired, testPrettyPrint(sample));
	}

	@Test
	public void alternateHeaderExtraLineTest() throws IOException {
		String filename = "setext-extra-line";
		String sample = loadFile(filename + SAMPLE);
		String desired = loadFile(filename + DESIRED);
		assertEquals(desired, testPrettyPrint(sample));
	}

	@Test
	public void unorderedListTest() throws IOException {
		String filename = "ul";
		String sample = loadFile(filename + SAMPLE);
		String desired = loadFile(filename + DESIRED);
		assertEquals(desired, testPrettyPrint(sample));
	}

	@Test
	public void multipleLineBreakTest() throws IOException {
		String filename = "multiple-line-break";
		String sample = loadFile(filename + SAMPLE);
		String desired = loadFile(filename + DESIRED);
		assertEquals(desired, testPrettyPrint(sample));
	}

}

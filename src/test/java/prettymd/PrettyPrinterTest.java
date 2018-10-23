package prettymd;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import com.pascal.prettymd.FormatPretty;

public class PrettyPrinterTest {
	
	static final String RESOURCE_PATH = "src/test/resources/";
	
    public static String loadFile(String filename) throws IOException {
		List<String> list = new LinkedList<String>();
		list = Files.readAllLines(Paths.get(RESOURCE_PATH + filename), StandardCharsets.UTF_8);
		String result = String.join("\n", list) + "\n";
		return result;
    }
	
	@Test
	public void headerTest() throws IOException 
	{
		String sample = loadFile("atx.sample");
		String desired = loadFile("atx.desired");
	    assertEquals(desired, FormatPretty.testPrettyPrint(sample));
	}
	
	@Test
	public void blockTest() throws IOException 
	{	
		String filename = "block";
		String sample = loadFile(filename + ".sample");
		String desired = loadFile(filename + ".desired");
	    assertEquals(desired, FormatPretty.testPrettyPrint(sample));
	}
	
	@Test
	public void horizontalRowTest() throws IOException 
	{
		String filename = "hr";
		String sample = loadFile(filename + ".sample");
		String desired = loadFile(filename + ".desired");
	    assertEquals(desired, FormatPretty.testPrettyPrint(sample));
	}
	
	@Test
	public void lineBreakTest() throws IOException 
	{
		String filename = "line-break";
		String sample = loadFile(filename + ".sample");
		String desired = loadFile(filename + ".desired");
	    assertEquals(desired, FormatPretty.testPrettyPrint(sample));
	}
	
	@Test
	public void mixedTest() throws IOException 
	{
		String filename = "mixed";
		String sample = loadFile(filename + ".sample");
		String desired = loadFile(filename + ".desired");
	    assertEquals(desired, FormatPretty.testPrettyPrint(sample));
	}
	
	@Test
	public void orderedListTest() throws IOException 
	{
		String filename = "ol";
		String sample = loadFile(filename + ".sample");
		String desired = loadFile(filename + ".desired");
	    assertEquals(desired, FormatPretty.testPrettyPrint(sample));
	}
	
	@Test
	public void orderedInUnorderdListTest() throws IOException 
	{
		String filename = "ol-in-ul";
		String sample = loadFile(filename + ".sample");
		String desired = loadFile(filename + ".desired");
	    assertEquals(desired, FormatPretty.testPrettyPrint(sample));
	}
	
	@Test
	public void paragraphTest() throws IOException 
	{
		String filename = "p";
		String sample = loadFile(filename + ".sample");
		String desired = loadFile(filename + ".desired");
	    assertEquals(desired, FormatPretty.testPrettyPrint(sample));
	}
	
	@Test
	public void alternateHeaderTest() throws IOException 
	{
		String filename = "setext";
		String sample = loadFile(filename + ".sample");
		String desired = loadFile(filename + ".desired");
	    assertEquals(desired, FormatPretty.testPrettyPrint(sample));
	}
	
	@Test
	public void alternateHeaderExtraLineTest() throws IOException 
	{
		String filename = "setext-extra-line";
		String sample = loadFile(filename + ".sample");
		String desired = loadFile(filename + ".desired");
	    assertEquals(desired, FormatPretty.testPrettyPrint(sample));
	}
	
	@Test
	public void unorderedListTest() throws IOException 
	{
		String filename = "ul";
		String sample = loadFile(filename + ".sample");
		String desired = loadFile(filename + ".desired");
	    assertEquals(desired, FormatPretty.testPrettyPrint(sample));
	}
	
}

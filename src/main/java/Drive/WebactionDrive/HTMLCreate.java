package Drive.WebactionDrive;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;


public class HTMLCreate extends Document {
	
	private static Tag tagDiv  = Tag.valueOf("div");
	private static Tag tagSpan = Tag.valueOf("span");
	private int childIndex = 0;
	
	public HTMLCreate(String testCase) throws IOException{
		super("");
		this.append(new String(Files.readAllBytes(Paths.get("template.html"))));
		this.append(String.format("<h3>%s</h3>",testCase));
		childIndex++;
	}
	
	public HTMLCreate() throws IOException{
		super("");
		this.append(new String(Files.readAllBytes(Paths.get("template.html"))));
	}
	

	public void addTestSuite(String num, String Name){
		
		Element div = new Element(tagDiv, "");
		div.addClass("testsuite");
		Element spanNum =  new Element(tagSpan, num);
		spanNum.attr("id", "num");
		spanNum.text(num);
		Element spanName = new Element(tagSpan, Name);
		spanName.attr("id", "name");
		spanName.text(Name);
		div.appendChild(spanNum);
		div.appendChild(spanName);
		this.appendChild(div);
		childIndex++;
	}
	
	public void addTestCase(String num, String Name, String state){

		Element div = new Element(tagDiv, "");
		div.addClass("testcase");
		Element spanNum =  new Element(tagSpan, num);
		spanNum.attr("id", "num");
		spanNum.text(num);
		Element spanName = new Element(tagSpan, Name);
		spanName.attr("id", "name");
		spanName.text(Name);
		Element spanState = new Element(tagSpan, state);
		spanState.attr("id", "isAutomated");
		spanState.text(state);
		div.appendChild(spanNum);
		div.appendChild(spanName);
		div.appendChild(spanState);
		this.child(childIndex).appendChild(div);
		
	}
	
	public static HTMLCreate parseTableOfContentsToHTML(InputStream googleContent) throws IOException{
		BufferedReader googleReader = new BufferedReader( new InputStreamReader(googleContent));
		String line;
		Matcher m;
		HTMLCreate html = new HTMLCreate();
		while ((line = googleReader.readLine()) !=null){
			if ((m = Pattern.compile("(?i)([0-9])\\s+Test Cases.*").matcher(line)).find()){
				String digit = m.group(1);
				while ((line = googleReader.readLine()) !=null){
					if ((m =Pattern.compile(String.format("(^%s\\.[0-9])\\s+(.*)",digit)).matcher(line)).find()){
						html.addTestSuite(m.group(1), m.group(2));
					}
					if ((m =Pattern.compile(String.format("(^%s\\.[0-9]\\.[0-9])\\s+(.*)",digit)).matcher(line)).find()){
						html.addTestCase(m.group(1), m.group(2), "not Automated");

					}
				}
			}
		}
		return html;
	}
	
}

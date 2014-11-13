package hu.qgears.review.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.VisitorSupport;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;

public class UtilDom4j {
	public static List<Element> selectElements(Element e, String path) {
		List<Element> ret = new ArrayList<Element>();
		for (Object o : e.selectNodes(path)) {
			ret.add((Element) o);
		}
		return ret;
	}
	public static List<Attribute> selectAttributes(Element e, String path) {
		List<Attribute> ret = new ArrayList<Attribute>();
		for (Object o : e.selectNodes(path)) {
			ret.add((Attribute) o);
		}
		return ret;
	}
	public static List<Element> detachElements(Element root, String path) {
		List<Element> ret=UtilDom4j.selectElements(root, path);
		for(Element e:ret)
		{
			e.detach();
		}
		return ret;
	}
	public static List<Attribute> detachAttributes(Element root, String path) {
		List<Attribute> ret=UtilDom4j.selectAttributes(root, path);
		for(Attribute e:ret)
		{
			e.detach();
		}
		return ret;
	}


	static public void write(OutputStream fos, Document document) throws IOException {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			// lets write to a file
			write(osw,document);
		} finally {
			fos.close();
		}
	}
	static public byte[] write(Document document) throws IOException {
		ByteArrayOutputStream fos=new ByteArrayOutputStream();
		try {
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			// lets write to a file
			write(osw,document);
		} finally {
			fos.close();
		}
		return fos.toByteArray();
	}
	static public void write(File out, Document document) throws IOException {
		FileOutputStream fos = new FileOutputStream(out);
		try {
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			// lets write to a file
			write(osw,document);
		} finally {
			fos.close();
		}
	}
	static public void write(PrintStream out, Document document) throws IOException {
		PrintWriter pw=new PrintWriter(out);
		write(pw, document);
	}
	static public void write(Writer osw, Document document) throws IOException {
		XMLWriter writer = new XMLWriter(osw, OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	public static Document read(File file) throws DocumentException, MalformedURLException {
		SAXReader reader = new SAXReader();
		URL url = file.toURI().toURL();
		return reader.read(url);
	}
	public static Document read(Reader r) throws DocumentException, MalformedURLException {
		SAXReader reader = new SAXReader();
		return reader.read(r);
	}
	public static Document read(InputStream is) throws DocumentException, MalformedURLException {
		SAXReader reader = new SAXReader();
		return reader.read(is);
	}
	public static Document read(URL url) throws DocumentException, MalformedURLException {
		SAXReader reader = new SAXReader();
		return reader.read(url);
	}
	public static void deleteElements(Element root, String selector)
	{
		for (Element e : UtilDom4j.selectElements(root, selector)) {
			e.detach();
		}
	}
	public static void overWriteAttribute(Element root, String selector, String attName, String newValue)
	{
		for (Element e : UtilDom4j.selectElements(
				root, selector)) {
			e.addAttribute(attName, newValue);
		}
	}

	public static void format(File f) throws MalformedURLException, IOException, DocumentException {
		write(f,read(f));
	}

	public static void copy(File src, File trg) throws MalformedURLException, IOException, DocumentException {
		write(trg,read(src));
	}
	public interface Visitor
	{

		void visit(Element modelRoot);
		
	}
	public static void visit(Element modelRoot, Visitor visitor) {
		visitor.visit(modelRoot);
		for(Object o:modelRoot.elements())
		{
			Element e=(Element) o;
			visit(e, visitor);
		}
	}
	public static String getAttributeValue(Element e, String nameSpaceUri, String attName) {
		// e.attributeValue(new QName(attName, new Namespace(nameSpace, null)))
		for(Object o:e.attributes())
		{
			if(o instanceof Attribute)
			{
				Attribute a=(Attribute)o;
				if(a.getName().equals(attName))
				{
					String uri=a.getNamespaceURI();
					if(uri==null||uri.equals(""))
					{
						if(nameSpaceUri==null||nameSpaceUri.equals(""))
						{
							return a.getValue();
						}
					}else
					{
						if(uri.equals(nameSpaceUri))
						{
							return a.getValue();
						}
					}
				}
			}
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * <p>
	 * Set the value of the attribute attName (with namespaceUri) in Element e.
	 * The supplied {@link Element} must be inside a {@link Document}. If not, a
	 * {@link NullPointerException} is thrown.
	 * </p>
	 * <p>
	 * If the attribute with the given name doesn't exist, it is created.
	 * </p>
	 * @param e
	 * @param nameSpaceUri
	 * @param attName
	 * @param value
	 */
	public static void setAttributeValue(Element e, String nameSpaceUri, String attName, String value){
		Namespace ns = e.getDocument().getRootElement().getNamespaceForURI(nameSpaceUri);
		QName qName = new QName(attName, ns);
		Attribute attr = e.attribute(qName);
		if(attr==null){
			e.addAttribute(qName, value);
		}else{
			attr.setValue(value);
		}
	}
	public static void cleanNamespace(Document doc)
	{
		doc.accept(new NameSpaceCleaner());
	}

	private static final class NameSpaceCleaner extends VisitorSupport {
		public void visit(Document document) {
			((DefaultElement) document.getRootElement())
					.setNamespace(Namespace.NO_NAMESPACE);
			document.getRootElement().additionalNamespaces().clear();
		}

		public void visit(Namespace namespace) {
			namespace.detach();
		}

		public void visit(Attribute node) {
			if (node.toString().contains("xmlns")
					|| node.toString().contains("xsi:")) {
				node.detach();
			}
		}

		public void visit(Element node) {
			if (node instanceof DefaultElement) {
				((DefaultElement) node).setNamespace(Namespace.NO_NAMESPACE);
			}
		}
	}
}

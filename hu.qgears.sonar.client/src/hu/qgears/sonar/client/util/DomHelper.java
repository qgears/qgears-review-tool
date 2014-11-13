package hu.qgears.sonar.client.util;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class DomHelper {
	
	private DomHelper (){}
	
	public static Element getChildElementByTagName(Element e,String tagName){
		NodeList l = e.getElementsByTagName(tagName);
		Element el = null;
		if (l.getLength() > 0){
			el = (Element) l.item(0);
		}
		return el;
	}
}

package com.lge.swiftdepends.infrastructure.persistence;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.ComponentRepository;
import com.lge.swiftdepends.domain.ComponentType;
import com.lge.swiftdepends.domain.Module;
import com.lge.swiftdepends.domain.Visitor;

public class LdiXmlRepository implements ComponentRepository, Visitor {
	private Path ldiXmlFilePath = null;
	private Document doc = null;
	private Element ldiElement = null;
	private HashMap<String, String> elementMap = null;
	private final String DELIMITER = "/";

	public LdiXmlRepository(Path ldiXmlFilePath) {
		super();
		this.ldiXmlFilePath = ldiXmlFilePath;
		elementMap = new HashMap<>();

		try {
			this.doc = createNewDocument();
		} catch (ParserConfigurationException e) {
			System.out.println("ERROR: Make LDI XML Document");
			e.printStackTrace();
			return;
		}
	}

	@Override
	public boolean create(List<Component> components) {
		// create "ldi" element
		elementMap.put("delimiter", DELIMITER);
		elementMap.put("ordering", "original");

		this.ldiElement = makeElement("ldi", elementMap);
		this.doc.appendChild(ldiElement);

		// create "elementtype" element
		addElementType("FILE", "swift file", "false");
		addElementType("PROTOCOL", "Protocol", "false");
		addElementType("CLASS", "Class", "false");
		addElementType("STRUCT", "Struct", "false");
		addElementType("ENUM", "Enum", "false");
		addElementType("FUNCTION", "Function", "true");
		addElementType("VARIABLE", "Variable", "true");

		// create "element" element
		components.forEach(component -> {
			component.accecpt(this);
		});

		try {
			writeXml(this.doc, new FileOutputStream(ldiXmlFilePath.toString()));
		} catch (FileNotFoundException | TransformerException e) {
			System.out.println("ERROR: Write LDI XML");
			return false;
		}

		return true;
	}

	@Override
	public List<Component> read() {
		throw new NotImplementedException("The read function of LdiXmlRepository is not implemented.");
	}

	private Document createNewDocument() throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		return doc;
	}

	private void addElementType(String type, String display, String member) {
		elementMap.put("type", type);
		elementMap.put("display", display);
		elementMap.put("member", member);
		this.ldiElement.appendChild(makeElement("elementtype", elementMap));
	}

	private Element addElement(String name, String type) {
		elementMap.put("name", name);
		elementMap.put("type", type);
		Element element = makeElement("element", elementMap);
		this.ldiElement.appendChild(element);
		return element;
	}

	private void addUses(Element element, String kind, String provider) {
		elementMap.put("kind", kind);
		elementMap.put("provider", provider);
		elementMap.put("strength", "1");
		element.appendChild(makeElement("uses", elementMap));
	}

	private Element makeElement(String name, Map<String, String> attributes) {
		Element element = this.doc.createElement(name);
		attributes.forEach((key, value) -> {
			element.setAttribute(key, value);
		});

		attributes.clear();
		return element;
	}

	private void writeXml(Document doc, OutputStream output) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(output);
		transformer.transform(source, result);
	}

	private String getPath(Component component) {
		try {
			return getPath(component.getParent()) + createPathForLattixDSMView(component);
		} catch (Exception e) {
			if (!component.getPath().equals(""))
				return component.getPath();
			else
				return createPathForLattixDSMView(component);
		}
	}

	private String createPathForLattixDSMView(Component component) {
		if (component.getType() == ComponentType.EXTERNAL)
			return "/" + component.getName().replace(".", "/");
		else
			return "/" + component.getName();
	}

	@Override
	public void visit(com.lge.swiftdepends.domain.Element element) {
		// External component does not create element.
		if (element.getType() == ComponentType.EXTERNAL)
			return;

		// add element
		org.w3c.dom.Element domElement = addElement(getPath(element), element.getType().toString());

		// add uses element
		element.getDependencies().forEach(dependency -> {
			addUses(domElement, dependency.getType().toString(), getPath(dependency.getDependsOnComponent()));
		});
	}

	@Override
	public void visit(Module module) {
		// External component does not create element.
		if (module.getType() == ComponentType.EXTERNAL)
			return;

		// add element
		org.w3c.dom.Element domElement = addElement(getPath(module), module.getType().toString());

		// add uses element
		module.getDependencies().forEach(dependency -> {
			addUses(domElement, dependency.getType().toString(), getPath(dependency.getDependsOnComponent()));
		});

		module.getComponents().forEach(component -> {
			component.accecpt(this);
		});
	}
}

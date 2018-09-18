package de.mannheim.ids.xslt;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Stack Extensible Stylesheet Transformations
 * so that they are executed in order,
 * and return result as String.
 * 
 * @author bfi
 *
 */
public class XSLTStacker {
	
	private Boolean formatting = false; 
	private SAXTransformerFactory stf = (SAXTransformerFactory) new net.sf.saxon.BasicTransformerFactory();

	private ArrayList<TransformerHandler> handlers = new ArrayList<TransformerHandler>();
	
	/**
	 * create potentially formatting XSLTStacker
	 * @param formattingB - if formatter is formatting 
	 * @throws TransformerConfigurationException
	 */
	public XSLTStacker(Boolean formattingB)
			throws TransformerConfigurationException {
		formatting = formattingB;
		TransformerHandler th = stf.newTransformerHandler();
		handlers.add(th);
		updateCurrentHandler();
	}

	/**
	 * creates non-formatting XSLTStacker
	 * @throws TransformerConfigurationException
	 */
	public XSLTStacker() throws TransformerConfigurationException {
		this(false);
	}
	
	/**
	 * Utility: Handle formatting if necessary.
	 * set formatting for end of pipeline,
	 * potentially reset formatting for previous end of pipeline.
	 */
	private void updateCurrentHandler() {
			TransformerHandler currentHandler = handlers.get(handlers.size() - 1);
		if (handlers.size() >= 2) {
			if (formatting) {
				handlers.get(handlers.size() - 2).getTransformer().setOutputProperty(OutputKeys.INDENT, "no");
			}
			handlers.get(handlers.size() - 2).setResult(new SAXResult(currentHandler));
		}
		if (formatting) {
			currentHandler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
			currentHandler.getTransformer().setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		}
	}


	/**
	 * add XSLT from stream
	 * @param stream
	 * @throws TransformerException
	 */
	public void addXSLT(InputStream stream) throws TransformerException {
	
		Templates templates = stf.newTemplates(new StreamSource(stream));
		handlers.add(stf.newTransformerHandler(templates));		
		updateCurrentHandler();
	}

	/**
	 * @param source
	 * @return String representation of transformation  ∏_{handlers}(source)
	 * @throws TransformerException
	 */
	public String transform(Source source) throws TransformerException {
		Transformer trans = stf.newTransformer();
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		handlers.get(handlers.size() - 1).setResult(result);
		trans.transform(source, new SAXResult(handlers.get(0)));
		return sw.toString();
	}
}
;
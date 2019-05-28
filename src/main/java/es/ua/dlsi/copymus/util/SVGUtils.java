package es.ua.dlsi.copymus.util;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.svg.SVGDocument;

public class SVGUtils {
	
	// http://my2iu.blogspot.com/2006/05/getting-svg-bounding-box-out-of-batik.html
	public static Rectangle2D getBoundingBox(String svgPath) throws IOException {
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		SVGDocument doc = (SVGDocument) f.createDocument(null, new FileInputStream(new File(svgPath)));
		GVTBuilder builder = new GVTBuilder();
		BridgeContext ctx;
		ctx = new BridgeContext(new UserAgentAdapter());
		GraphicsNode gvtRoot = builder.build(ctx, doc);
		return gvtRoot.getSensitiveBounds();
	}

	// https://stackoverflow.com/questions/6875807/convert-svg-to-pdf
	// http://svn.apache.org/viewvc/xmlgraphics/fop/trunk/fop/examples/embedding/java/embedding/ExampleSVG2PDF.java?view=markup
	public static byte[] svg2pdf(String svgPath) throws IOException, TranscoderException {
		Rectangle2D rect = getBoundingBox(svgPath);

		PDFTranscoder transcoder = new PDFTranscoder();
		
		// This is necessary to prevent an error due to a conflict in dependencies
		// PDFTranscoder (as inherited from AbstractFOPTransformer) creates a JCL SimpleLog instance
		//   if no other logger is provided. SimpleLog is provided by two different packages:
		//   spring-jcl (included by spring-boot-starter-test) and commons-logging (included by fop).
		//   SimpleLog has been deprecated in spring-jcl and its implementation is empty,
		//   and this class supersedes the one provided by commons-logging,
		//   resulting in a runtime error when transcode() is invoked due to the setLevel() method missing
		transcoder.setLogger(LogFactory.getLog(PDFTranscoder.class));

		// http://www.nccp.org/lib/batik/docs/rasterizerTutorial.html#selectAreaOfIntrest
		transcoder.addTranscodingHint(PDFTranscoder.KEY_WIDTH, (float)rect.getWidth());
		transcoder.addTranscodingHint(PDFTranscoder.KEY_HEIGHT, (float)rect.getHeight());
		transcoder.addTranscodingHint(PDFTranscoder.KEY_AOI, rect);
		
		TranscoderInput transcoderInput;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		TranscoderOutput transcoderOutput = new TranscoderOutput(output);
		byte[] pdf = {};
		try {
			transcoderInput = new TranscoderInput(new FileInputStream(new File(svgPath))); 
			transcoder.transcode(transcoderInput, transcoderOutput);
			output.flush();
			pdf = output.toByteArray();
		} finally {
			output.close();
		}

		return pdf;
	}

	public static byte[] svg2png(String svgPath) throws IOException, TranscoderException {
		Rectangle2D rect = getBoundingBox(svgPath);

		PNGTranscoder transcoder = new PNGTranscoder();
		
		// http://www.nccp.org/lib/batik/docs/rasterizerTutorial.html#selectAreaOfIntrest
		transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float)rect.getWidth());
		transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float)rect.getHeight());
		transcoder.addTranscodingHint(PNGTranscoder.KEY_AOI, rect);
		
		TranscoderInput transcoderInput;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		TranscoderOutput transcoderOutput = new TranscoderOutput(output);
		byte[] pdf = {};
		try {
			transcoderInput = new TranscoderInput(new FileInputStream(new File(svgPath))); 
			transcoder.transcode(transcoderInput, transcoderOutput);
			output.flush();
			pdf = output.toByteArray();
		} finally {
			output.close();
		}

		return pdf;
	}
}

package org.fiteagle.delivery.xmlrpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import redstone.xmlrpc.XmlRpcDispatcher;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.XmlRpcInvocation;
import redstone.xmlrpc.XmlRpcInvocationHandler;
import redstone.xmlrpc.XmlRpcInvocationInterceptor;
import redstone.xmlrpc.XmlRpcMessages;
import redstone.xmlrpc.XmlRpcServer;

public class FixedXmlRpcDispatcher extends XmlRpcDispatcher {

	public static String DEFAULT_HANDLER_NAME = "__default__";
	private Writer writer;
	XmlRpcServer server;
	private int callSequence;
	@SuppressWarnings("rawtypes")
	private List arguments = new ArrayList<>(6);
	String methodName;

	public FixedXmlRpcDispatcher(XmlRpcServer server, String client_ip) {
		super(server, client_ip);
		this.server = server;

	}

	@Override
	public void dispatch(InputStream xmlInput, Writer xmlOutput)
			throws XmlRpcException {
		// Parse the inbound XML-RPC message. May throw an exception.
		parse(xmlInput);
		this.writer = xmlOutput;
		int i = this.methodName.lastIndexOf(".");
		if (i == -1) {
			this.methodName = DEFAULT_HANDLER_NAME + "." + this.methodName;
			i = this.methodName.lastIndexOf(".");
		}
		if (i > -1) {
			String str = this.methodName.substring(0, i);
			this.methodName = this.methodName.substring(i + 1);
			XmlRpcInvocationHandler localXmlRpcInvocationHandler = this.server
					.getInvocationHandler(str);
			if (localXmlRpcInvocationHandler != null) {
				int j = ++callSequence;
				XmlRpcInvocation localXmlRpcInvocation = null;
				if (this.server.getInvocationInterceptors().size() > 0)
					localXmlRpcInvocation = new XmlRpcInvocation(j, str,
							this.methodName, localXmlRpcInvocationHandler,
							this.arguments, this.writer);
				try {
					if (!preProcess(localXmlRpcInvocation)) {
						writeError(
								-1,
								XmlRpcMessages
										.getString("XmlRpcDispatcher.InvocationCancelled"));
					} else {
						Object localObject = localXmlRpcInvocationHandler
								.invoke(localXmlRpcInvocation.getMethodName(),
										this.arguments);
						localObject = postProcess(localXmlRpcInvocation,
								localObject);
						if (localObject != null)
							writeValue(localObject);
					}
				} catch (Throwable localThrowable) {
					processException(localXmlRpcInvocation, localThrowable);
					int k = -1;
					if ((localThrowable instanceof XmlRpcFault))
						k = ((XmlRpcFault) localThrowable).getErrorCode();
					writeError(k, localThrowable.getClass().getName() + ": "
							+ localThrowable.getMessage());
				}
			} else {
				writeError(-1,
						XmlRpcMessages
								.getString("XmlRpcDispatcher.HandlerNotFound"));
			}
		} else {
			writeError(
					-1,
					XmlRpcMessages
							.getString("XmlRpcDispatcher.InvalidMethodNameFormat"));
		}

	}

	/**
	 * Invokes all processor objects registered with the XmlRpcServer this
	 * dispatcher is working for.
	 * 
	 * @todo Determine a way for a preProcess call to indicate the reason for
	 *       cancelling the invocation.
	 * 
	 * @return true if the invocation should continue, or false if the
	 *         invocation should be cancelled for some reason.
	 */
	private boolean preProcess(XmlRpcInvocation invocation) {
		XmlRpcInvocationInterceptor p;

		for (int i = 0; i < server.getInvocationInterceptors().size(); ++i) {
			p = (XmlRpcInvocationInterceptor) server
					.getInvocationInterceptors().get(i);

			if (!p.before(invocation)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Invokes all interceptor objects registered with the XmlRpcServer this
	 * dispatcher is working for.
	 */

	private Object postProcess(XmlRpcInvocation invocation, Object returnValue) {
		XmlRpcInvocationInterceptor p;

		for (int i = 0; i < server.getInvocationInterceptors().size(); ++i) {
			p = (XmlRpcInvocationInterceptor) server
					.getInvocationInterceptors().get(i);
			returnValue = p.after(invocation, returnValue);

			// If the interceptor intercepts the return value completely and
			// takes
			// responsibility for writing a response directly to the client,
			// break
			// the interceptor chain and return immediately.

			if (returnValue == null) {
				return null;
			}
		}

		return returnValue;
	}

	/**
	 * Invokes all processor objects registered with the XmlRpcServer this
	 * dispatcher is working for.
	 */

	private void processException(XmlRpcInvocation invocation,
			Throwable exception) {
		XmlRpcInvocationInterceptor p;

		for (int i = 0; i < server.getInvocationInterceptors().size(); ++i) {
			p = (XmlRpcInvocationInterceptor) server
					.getInvocationInterceptors().get(i);

			p.onException(invocation, exception);
		}
	}

	/**
	 * Writes a return value to the XML-RPC writer.
	 * 
	 * @param value
	 *            The value to be encoded into the writer.
	 * @throws IOException
	 */

	private void writeValue(Object value) throws IOException {
		server.getSerializer().writeEnvelopeHeader(value, writer);

		if (value != null) {
			server.getSerializer().serialize(value, writer);
		}

		server.getSerializer().writeEnvelopeFooter(value, writer);
	}

	/**
	 * Creates an XML-RPC fault struct and puts it into the writer buffer.
	 * 
	 * @param message
	 *            The fault string.
	 */

	private void writeError(int paramInt, String paramString) {
		try {

			this.server.getSerializer().writeError(paramInt, paramString,
					this.writer);
		} catch (IOException localIOException) {
			System.err.println(localIOException.getMessage());
		}
	}

	public void endElement(String paramString1, String paramString2,
			String paramString3) throws SAXException {
		if (paramString2.equals("methodName"))
			this.methodName = consumeCharData();
		else
			super.endElement(paramString1, paramString2, paramString3);
	}

	@SuppressWarnings("unchecked")
	protected void handleParsedValue(Object paramObject) {
		this.arguments.add(paramObject);
	}
}
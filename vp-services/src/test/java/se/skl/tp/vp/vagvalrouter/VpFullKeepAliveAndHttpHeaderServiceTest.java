/**
 * Copyright (c) 2013 Center for eHalsa i samverkan (CeHis).
 * 							<http://cehis.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skl.tp.vp.vagvalrouter;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.mule.tck.FunctionalTestCase;

import se.skl.tp.vp.util.VPUtil;
import se.skl.tp.vp.vagvalagent.SokVagvalsInfoMockInput;
import se.skl.tp.vp.vagvalagent.VagvalMockInputRecord;

@SuppressWarnings("deprecation")
public class VpFullKeepAliveAndHttpHeaderServiceTest extends FunctionalTestCase {

	
	private HttpClient client;
	private SslProtocolSocketFactory socketFactory;

	
	public VpFullKeepAliveAndHttpHeaderServiceTest() {
		super();
		setDisposeManagerPerSuite(true);
		
		SokVagvalsInfoMockInput svimi = new SokVagvalsInfoMockInput();
		List<VagvalMockInputRecord> vagvalInputs = new ArrayList<VagvalMockInputRecord>();
		VagvalMockInputRecord vi = new VagvalMockInputRecord();
		vi.receiverId = "vp-test-producer";
		vi.senderId = "tp";
		vi.rivVersion = "RIVTABP20";
		vi.serviceNamespace = "urn:skl:tjanst1:rivtabp20";
		vi.adress = "https://localhost:19000/vardgivare-b/tjanst1";
		vagvalInputs.add(vi);
		svimi.setVagvalInputs(vagvalInputs);
		
	}
	
	@Override
	protected String getConfigResources() {
		return 
			"soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
			"vp-common.xml," + 
			"services/VagvalRouter-service.xml," +
			"vp-teststubs-and-services-config.xml";
	}
	
	@Override
	protected void doSetUp() throws Exception {
		InputStream keyStore = getClass().getClassLoader().getResourceAsStream("certs/client.jks");
		InputStream trustStore = getClass().getClassLoader().getResourceAsStream("certs/truststore.jks");
		
		socketFactory = new SslProtocolSocketFactory(keyStore, "password", trustStore, "password");
		
		Protocol authhttps = new Protocol("https", socketFactory, 20000);

		client = new HttpClient();
		client.getHostConfiguration().setHost("localhost", 20000, authhttps);
		
	}
		
	@Override
	protected void doTearDown() throws Exception {
		
		socketFactory.getSocket().close();
	}
	
	public void testHttp11ToKeepAliveEndpoint() throws Exception {

		PostMethod httppost = executeHttp11SoapCall("/vp/keep-alive-tjanst1");

		assertNull(httppost.getResponseHeader("Connection"));
		assertTrue(httppost.getResponseBodyAsString().contains("listProductsResponse"));
		assertEquals(200, httppost.getStatusCode());
		
		executeHttp11SoapCall("/vp/keep-alive-tjanst1");		
	}
	
	public void testHttp11KeepAliveToKeepAliveEndpoint() throws Exception {

		PostMethod httppost = executeHttp11SoapCallWithKeepAlive("/vp/keep-alive-tjanst1");

		assertNull(httppost.getResponseHeader("Connection"));
		assertTrue(httppost.getResponseBodyAsString().contains("listProductsResponse"));
		assertEquals(200, httppost.getStatusCode());
		
		executeHttp11SoapCall("/vp/keep-alive-tjanst1");
	}
	
	public void testHttp11CloseToKeepAliveEndpoint() throws Exception {

		PostMethod httppost = executeHttp11SoapCallWithClose("/vp/keep-alive-tjanst1");
		
		assertNotNull(httppost.getResponseHeader("Connection"));
		assertEquals("close", httppost.getResponseHeader("Connection").getValue());
		assertTrue(httppost.getResponseBodyAsString().contains("listProductsResponse"));
		assertEquals(200, httppost.getStatusCode());
		
		try {
			executeHttp11SoapCall("/vp/keep-alive-tjanst1");
			fail("Expected Socket Exception!!");
		} catch (SocketException se) {
			assertEquals("Socket is closed", se.getMessage());
		}		
	}
	
	public void testHttp11ToNonKeepAliveEndpoint() throws Exception {

		PostMethod httppost = executeHttp11SoapCallWithClose("/vp/no-keep-alive-tjanst1");
			
		assertNotNull(httppost.getResponseHeader("Connection"));
		assertEquals("close", httppost.getResponseHeader("Connection").getValue());
		assertTrue(httppost.getResponseBodyAsString().contains("listProductsResponse"));
		assertEquals(200, httppost.getStatusCode());
		
		try {
			executeHttp11SoapCall("/vp/no-keep-alive-tjanst1");
			fail("Expected Socket Exception!!");
		} catch (SocketException se) {
			assertEquals("Socket is closed", se.getMessage());
		}		
	}
	
	public void testHttp11KeepAliveToNonKeepAliveEndpoint() throws Exception {

		PostMethod httppost = executeHttp11SoapCallWithKeepAlive("/vp/no-keep-alive-tjanst1");
			
		assertNotNull(httppost.getResponseHeader("Connection"));
		assertEquals("close", httppost.getResponseHeader("Connection").getValue());
		assertTrue(httppost.getResponseBodyAsString().contains("listProductsResponse"));
		assertEquals(200, httppost.getStatusCode());
		
		try {
			executeHttp11SoapCall("/vp/no-keep-alive-tjanst1");
			fail("Expected Socket Exception!!");
		} catch (SocketException se) {
			assertEquals("Socket is closed", se.getMessage());
		}		
	}
	
	public void testHttp11CloseToNonKeepAliveEndpoint() throws Exception {

		PostMethod httppost = executeHttp11SoapCallWithClose("/vp/no-keep-alive-tjanst1");
		
		assertNotNull(httppost.getResponseHeader("Connection"));
		assertEquals("close", httppost.getResponseHeader("Connection").getValue());
		assertTrue(httppost.getResponseBodyAsString().contains("listProductsResponse"));
		assertEquals(200, httppost.getStatusCode());
		
		try {
			executeHttp11SoapCall("/vp/no-keep-alive-tjanst1");
			fail("Expected Socket Exception!!");
		} catch (SocketException se) {
			assertEquals("Socket is closed", se.getMessage());
		}		
	}
	
	public void testHttp10ToKeepAliveEndpoint() throws Exception {

		PostMethod httppost = executeHttp10SoapCall("/vp/keep-alive-tjanst1");

		assertNotNull(httppost.getResponseHeader("Connection"));
		assertEquals("close", httppost.getResponseHeader("Connection").getValue());
		assertTrue(httppost.getResponseBodyAsString().contains("listProductsResponse"));
		assertEquals(200, httppost.getStatusCode());
		
		try {
			executeHttp10SoapCall("/vp/keep-alive-tjanst1");
		} catch (SocketException se){
			assertEquals("Socket is closed", se.getMessage());			
		}	
	}
	
	/*
	public void testHttp10KeepAliveToKeepAliveEndpoint() throws Exception {

		PostMethod httppost = executeHttp10SoapCallWithKeepAlive("/vp/keep-alive-tjanst1");

		assertNotNull(httppost.getResponseHeader("Connection"));
		assertEquals("keep-alive", httppost.getResponseHeader("Connection").getValue());
		assertTrue(httppost.getResponseBodyAsString().contains("listProductsResponse"));
		assertEquals(200, httppost.getStatusCode());
		
		executeHttp10SoapCall("/vp/keep-alive-tjanst1");
	}
	*/
	
	public void testHttp10CloseToKeepAliveEndpoint() throws Exception {

		PostMethod httppost = executeHttp10SoapCallWithClose("/vp/keep-alive-tjanst1");
		
		assertNotNull(httppost.getResponseHeader("Connection"));
		assertEquals("close", httppost.getResponseHeader("Connection").getValue());
		assertTrue(httppost.getResponseBodyAsString().contains("listProductsResponse"));
		assertEquals(200, httppost.getStatusCode());
		
		try {
			executeHttp10SoapCall("/vp/keep-alive-tjanst1");
			fail("Expected Socket Exception!!");
		} catch (SocketException se) {
			assertEquals("Socket is closed", se.getMessage());
		}
	}
	
	public void testHttp10ToNonKeepAliveEndpoint() throws Exception {

		PostMethod httppost = executeHttp10SoapCallWithClose("/vp/no-keep-alive-tjanst1");
			
		assertNotNull(httppost.getResponseHeader("Connection"));
		assertEquals("close", httppost.getResponseHeader("Connection").getValue());
		assertTrue(httppost.getResponseBodyAsString().contains("listProductsResponse"));
		assertEquals(200, httppost.getStatusCode());
		
		try {
			executeHttp10SoapCall("/vp/no-keep-alive-tjanst1");
			fail("Expected Socket Exception!!");
		} catch (SocketException se) {
			assertEquals("Socket is closed", se.getMessage());
		}		
	}
	
	public void testHttp10KeepAliveToNonKeepAliveEndpoint() throws Exception {

		PostMethod httppost = executeHttp10SoapCallWithKeepAlive("/vp/no-keep-alive-tjanst1");
			
		assertNotNull(httppost.getResponseHeader("Connection"));
		assertEquals("close", httppost.getResponseHeader("Connection").getValue());
		assertTrue(httppost.getResponseBodyAsString().contains("listProductsResponse"));
		assertEquals(200, httppost.getStatusCode());
		
		try {
			executeHttp10SoapCall("/vp/no-keep-alive-tjanst1");
			fail("Expected Socket Exception!!");
		} catch (SocketException se) {
			assertEquals("Socket is closed", se.getMessage());
		}		
	}
	
	public void testHttp10CloseToNonKeepAliveEndpoint() throws Exception {

		PostMethod httppost = executeHttp10SoapCallWithClose("/vp/no-keep-alive-tjanst1");
		
		assertNotNull(httppost.getResponseHeader("Connection"));
		assertEquals("close", httppost.getResponseHeader("Connection").getValue());
		assertTrue(httppost.getResponseBodyAsString().contains("listProductsResponse"));
		assertEquals(200, httppost.getStatusCode());
		
		try {
			executeHttp10SoapCall("/vp/no-keep-alive-tjanst1");
			fail("Expected Socket Exception!!");
		} catch (SocketException se) {
			assertEquals("Socket is closed", se.getMessage());
		}
	}
	
	public void testProducerResponseTimeIsReturnedWhenHttp200() throws Exception {
        PostMethod httppost = executeHttp11SoapCall("/vp/keep-alive-tjanst1");
        assertNotNull(httppost.getResponseHeader(VagvalRouter.X_SKLTP_PRODUCER_RESPONSETIME).getValue());
        assertEquals(200, httppost.getStatusCode());
    }
	
	public void testProducerResponseTimeIsReturnedWhenHttp500() throws Exception {
        PostMethod httppost = executeHttp11SoapCallReturningHttp500("/vp/keep-alive-tjanst1");
        assertNotNull(httppost.getResponseHeader(VagvalRouter.X_SKLTP_PRODUCER_RESPONSETIME).getValue());
        assertEquals(500, httppost.getStatusCode());
    }
	
	private PostMethod executeSoapCall(final String subUrl, final boolean doClose, final boolean doKeepAlive, final boolean version10) throws HttpException, IOException {
		PostMethod httppost = new PostMethod(subUrl);
		RequestEntity requestEntity = new InputStreamRequestEntity(getClass().getClassLoader().getResourceAsStream("testfiles/ListProducts.xml"));
		httppost.getParams().setVersion(version10 ? HttpVersion.HTTP_1_0 : HttpVersion.HTTP_1_1);
		httppost.setRequestEntity(requestEntity);
		if(doClose) httppost.setRequestHeader("Connection", "close");
		if(doKeepAlive) httppost.setRequestHeader("Connection", "keep-alive");
		client.executeMethod(httppost);
		return httppost;
	}
	
	private PostMethod executeSoapCallHttp500(final String subUrl, final boolean doClose, final boolean doKeepAlive, final boolean version10) throws HttpException, IOException {
        PostMethod httppost = new PostMethod(subUrl);
        RequestEntity requestEntity = new InputStreamRequestEntity(getClass().getClassLoader().getResourceAsStream("testfiles/GetProductsExceptionResponse.xml"));
        httppost.getParams().setVersion(version10 ? HttpVersion.HTTP_1_0 : HttpVersion.HTTP_1_1);
        httppost.setRequestEntity(requestEntity);
        if(doClose) httppost.setRequestHeader("Connection", "close");
        if(doKeepAlive) httppost.setRequestHeader("Connection", "keep-alive");
        client.executeMethod(httppost);
        return httppost;
    }
	
	private PostMethod executeHttp11SoapCallReturningHttp500(final String subUrl) throws HttpException, IOException {
        return executeSoapCallHttp500(subUrl, false, false, false);
    }

	private PostMethod executeHttp11SoapCallWithClose(final String subUrl) throws HttpException, IOException {
		return executeSoapCall(subUrl, true, false, false);
	}

	private PostMethod executeHttp11SoapCallWithKeepAlive(final String subUrl) throws HttpException, IOException {
		return executeSoapCall(subUrl, false, true, false);
	}

	private PostMethod executeHttp11SoapCall(final String subUrl) throws HttpException, IOException {
		return executeSoapCall(subUrl, false, false, false);
	}
	
	private PostMethod executeHttp10SoapCallWithClose(final String subUrl) throws HttpException, IOException {
		return executeSoapCall(subUrl, true, false, true);
	}

	private PostMethod executeHttp10SoapCallWithKeepAlive(final String subUrl) throws HttpException, IOException {
		return executeSoapCall(subUrl, false, true, true);
	}

	private PostMethod executeHttp10SoapCall(final String subUrl) throws HttpException, IOException {
		return executeSoapCall(subUrl, false, false, true);
	}
	
}
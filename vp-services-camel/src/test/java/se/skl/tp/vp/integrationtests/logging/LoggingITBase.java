package se.skl.tp.vp.integrationtests.logging;

import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_HTTP;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.createGetCertificateRequest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.integrationtests.utils.MockProducer;
import se.skl.tp.vp.integrationtests.utils.TestConsumer;
import se.skl.tp.vp.util.TestLogAppender;

public class LoggingITBase {
	public static final String HTTP_PRODUCER_URL = "http://localhost:19000/vardgivare-b/tjanst2";

	@Autowired
	TestConsumer consumer;

	@Autowired
	MockProducer producer;

	@Value("${vp.instance.id}")
	String vpInstanceId;

	Map<String, Object> headers = new HashMap<>();

	String httpRequest;

	@Before
	public void before() throws Exception {
		TestLogAppender.getInstance();

		producer.start(HTTP_PRODUCER_URL);
		producer.setResponseBody("foobar");

		TestLogAppender.clearEvents();

		headers.put(HttpHeaders.X_VP_INSTANCE_ID, vpInstanceId);
		headers.put(HttpHeaders.X_VP_SENDER_ID, "SenderWithDefaultBehorighet");

		httpRequest = createGetCertificateRequest(RECEIVER_HTTP);
	}
}
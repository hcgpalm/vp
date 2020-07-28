package se.skl.tp.vp.integrationtests.logging;

import static org.apache.camel.test.junit4.TestSupport.assertStringContains;
import static org.junit.Assert.assertFalse;

import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.integrationtests.utils.StartTakService;
import se.skl.tp.vp.logging.MessageInfoLogger;
import se.skl.tp.vp.util.TestLogAppender;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@StartTakService
public class PayloadLoggingIT extends LoggingITBase {


	@Test
	public void testPayloadLogging() throws Exception {
	    consumer.sendHttpRequestToVP(httpRequest, headers);

		String respOutLogMsg = TestLogAppender.getEventMessage(MessageInfoLogger.RESP_OUT, 0);
		assertStringContains(respOutLogMsg, "\nPayload=foobar\n");
		assertFalse("Text: " + respOutLogMsg + " contains PayloadTruncated",
				respOutLogMsg.contains("PayloadTruncated"));
	}
}
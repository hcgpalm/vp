package se.skl.tp.vp.integrationtests.certificate;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.httpheader.SenderIpExtractor;

@RunWith( CamelSpringBootRunner.class )
@ContextConfiguration(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
public class  CertificateReaderIT  extends CamelTestSupport {

  @EndpointInject(uri = "mock:result")
  protected MockEndpoint resultEndpoint;

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  @Autowired
  SenderIpExtractor senderIpExtractor;

  @Value("${" + PropertyConstants.VAGVALROUTER_SENDER_IP_ADRESS_HTTP_HEADER + "}")
  String forwardedHeader;

  @Test
  public void extractIPFromNettyHeader() throws Exception {
    String expectedBody = "test";

    resultEndpoint.expectedBodiesReceived(expectedBody);
    resultEndpoint.expectedPropertyReceived(VPExchangeProperties.SENDER_IP_ADRESS, "127.0.0.1");

    template.sendBody(expectedBody);
    resultEndpoint.assertIsSatisfied();
  }

  @Test
  public void extractIPFromForwardedHeader() throws Exception {
    String expectedBody = "test";

    resultEndpoint.expectedBodiesReceived(expectedBody);
    resultEndpoint.expectedPropertyReceived(VPExchangeProperties.SENDER_IP_ADRESS, "127.1.1.1");

    template.sendBodyAndHeader(expectedBody, forwardedHeader, "127.1.1.1");
    resultEndpoint.assertIsSatisfied();
  }

  @Override
  protected RouteBuilder createRouteBuilder() {
    return new RouteBuilder() {
      public void configure() {
        from("direct:start")
            .to("netty4-http:http://localhost:12123/vp");

        from("netty4-http:http://localhost:12123/vp")
            .process((Exchange exchange) -> {
              String senderIpAdress = senderIpExtractor.getSenderIpAdress(exchange.getIn());
              exchange.setProperty(VPExchangeProperties.SENDER_IP_ADRESS, senderIpAdress);
            })
            .to("mock:result");
      }
    };
  }
}

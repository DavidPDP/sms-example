package com.company.smspub.core;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.smpp.SmppConstants;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.engine.DefaultProducerTemplate;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class SmsPubManagment {

	/** Initial endpoint URI where the messages are sent. */
	private static final String START_ENDPOINT = "direct:start"; 
	
	private ProducerTemplate template;
	private CamelContext context;
	
	public SmsPubManagment(String propertiesPath) throws Exception {
		initSmppConnection(propertiesPath);
		pushMessage("replace with some msisdn","Space Oddity");
		tearDown();
	}
	
	private void initSmppConnection(String propertiesPath) throws Exception {
		
		Configuration config = loadApplicationProperties(propertiesPath);
		
		// Init context
		context = new DefaultCamelContext();
		context.addRoutes(getSmppRoute(config));
		context.start();
		
		// Init template
		template = new DefaultProducerTemplate(context);
		template.start();
		
	}
	
	private Configuration loadApplicationProperties(String path) 
			throws ConfigurationException {
		Configurations configs = new Configurations();
		return configs.properties(path);
	}
	
	/**
	 * This Java DSL allows defining a pipeline in which the
	 * message will be processed. Initially, the pipeline consists
	 * of sending messages to the SMSC through the SMPP protocol,
	 * but it can be extended to any necessary processing.
	 * 
	 * @param config instance where application properties can be obtained.
	 * 
	 * @return the messaging pipeline definition.
	 */
	private RouteBuilder getSmppRoute(Configuration config) {
		String smsEndPoint = getSmsEndPoint(config);
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(START_ENDPOINT).to(smsEndPoint);
			}
		};
	}
	
	private String getSmsEndPoint(Configuration config) {
		// Syntax: smpp://[systemId]@[host]:[port]?systemType=producer&
		//   sourceAddr=890105&password=[password]&sourceAddrTon=[sourceTon]&
		//   sourceAddrNpi=[sourceNpi]&destAddrTon=[destTon]&destAddrNpi=[destNpi]
		return "smpp://" + config.getString("company.smspub.systemId") + "@" +
			config.getString("company.smspub.host") + ":" +
			config.getString("company.smspub.port") + "?" + 
			"systemType=producer&sourceAddr=replace with source addr" +
			"&password=" + config.getString("company.smspub.password") + 
			"&sourceAddrTon=" + config.getString("company.smspub.sourceAddrTon") +
			"&sourceAddrNpi=" + config.getString("company.smspub.sourceAddrNpi") +
			"&destAddrTon=" + config.getString("company.smspub.destAddrTon") +
			"&destAddrNpi=" + config.getString("company.smspub.destAddrNpi");
	}
	
	private void pushMessage(String msisdn, String content) { 
		
		Exchange exchange = ExchangeBuilder.anExchange(context)
			// The destination header (msisdn) is overwritten because it's dynamic.
			.withHeader(SmppConstants.DEST_ADDR, msisdn)
			.withBody(content)
			.build();
		
		template.send(START_ENDPOINT, exchange);
		
	}
	
	private void tearDown() {
		context.stop();
		template.stop();
	}
	
}

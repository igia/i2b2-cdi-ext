/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.
 * 2.0 with a Healthcare Disclaimer.
 * A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
 * be found under the top level directory, named LICENSE.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * If a copy of the Healthcare Disclaimer was not distributed with this file, You
 * can obtain one at the project website https://github.com/igia.
 *
 * Copyright (C) 2018-2019 Persistent Systems, Inc.
 */
package io.igia.i2b2.cdi.dataimport.messagehandler;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class SftpOutboundMessageHandlerData {

	private static final Logger log = LoggerFactory.getLogger(SftpOutboundMessageHandlerData.class);

	public boolean sftpOutboundChannelData(String file) {
		boolean sendFile = false;
		try {
			File f = new File(file);
			Message<File> message = MessageBuilder.withPayload(f).build();
			sendFile = sftpOutboundChannelData().send(message);
		} catch (Exception e) {
			log.error("Error while sending data error log file to sftp using outbound channel adapter", e);
		}
		return sendFile;
	}

	@Bean
	public MessageChannel sftpOutboundChannelData() {
		return new DirectChannel();
	}
}
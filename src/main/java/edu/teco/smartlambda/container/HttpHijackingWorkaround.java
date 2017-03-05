/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package edu.teco.smartlambda.container;

import com.spotify.docker.client.LogReader;
import com.spotify.docker.client.LogStream;

import java.io.FilterInputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a workaround for lack of HTTP Hijacking support in Apache
 * HTTPClient. The assumptions made in Apache HTTPClient are that a
 * response is an InputStream and so we have no sane way to access the
 * underlying OutputStream (which exists at the socket level)
 * <p>
 * References :
 * https://docs.docker.com/reference/api/docker_remote_api_v1.16/#32-hijacking
 * https://github.com/docker/docker/issues/5933
 * <p>
 * This document was altered to work with the latest versions of spotify.docker, apache.httpclient and all their dependencies
 */
public class HttpHijackingWorkaround {
	
	public static WritableByteChannel getOutputStream(LogStream stream, String uri) throws Exception {
		final String[] fields =
				new String[] {"reader", "stream", "original", "input", "in", "in", "in", "eofWatcher", "wrappedEntity", "content", "in",
				              "instream"};
		final String[] declared = new String[] {"com.spotify.docker.client.DefaultLogStream", LogReader.class.getName(),
		                                        "org.glassfish.jersey.message.internal.ReaderInterceptorExecutor$UnCloseableInputStream",
		
		                                        "org.glassfish.jersey.message.internal.EntityInputStream",
		                                        FilterInputStream.class.getName(), FilterInputStream.class.getName(),
		                                        FilterInputStream.class.getName(), "org.apache.http.conn.EofSensorInputStream",
		                                        "org.apache.http.entity.HttpEntityWrapper", "org.apache.http.entity.BasicHttpEntity",
		                                        "org.apache.http.impl.io.IdentityInputStream",
		                                        "org.apache.http.impl.io.SessionInputBufferImpl"};
		final String[] bundles = new String[] {"org.glassfish.jersey.core.jersey-common", "org.apache.httpcomponents.httpcore",
		                                       "org.apache.httpcomponents.httpclient"};
		
		List<String[]> list = new LinkedList<>();
		for (int i = 0; i < fields.length; i++) {
			list.add(new String[] {declared[i], fields[i]});
		}
		
		if (uri.startsWith("unix:")) {
			list.add(new String[] {"sun.nio.ch.ChannelInputStream", "ch"});  //$NON-NLS-2$
		} else if (uri.startsWith("https:")) {
			float  jvmVersion = Float.parseFloat(System.getProperty("java.specification.version"));
			String fName;
			if (jvmVersion < 1.9f) {
				fName = "c";
			} else {
				fName = "socket";
			}
			list.add(new String[] {"sun.security.ssl.AppInputStream", fName});
		} else {
			list.add(new String[] {"java.net.SocketInputStream", "socket"});  //$NON-NLS-2$
		}
		
		Object res = getInternalField(stream, list, bundles);
		if (res instanceof WritableByteChannel) {
			return (WritableByteChannel) res;
		} else if (res instanceof Socket) {
			return Channels.newChannel(((Socket) res).getOutputStream());
		} else {
			throw new AssertionError(
					"Expected " + WritableByteChannel.class.getName() + " or " + Socket.class.getName() + " but found: " + "" +
							res.getClass().getName());
		}
	}
	
	/*
	 * Access arbitrarily nested internal fields.
	 */
	private static Object getInternalField(Object input, List<String[]> set, String[] bundles) {
		Object curr = input;
		try {
			for (String[] e : set) {
				Field f = loadClass(e[0], bundles).getDeclaredField(e[1]);
				f.setAccessible(true);
				curr = f.get(curr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return curr;
	}
	
	/*
	 * Avoid explicitly depending on certain classes that are requirements
	 * of the docker-client library (com.spotify.docker.client).
	 */
	private static Class<?> loadClass(String key, String[] bundles) {
		try {
			return Class.forName(key);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}

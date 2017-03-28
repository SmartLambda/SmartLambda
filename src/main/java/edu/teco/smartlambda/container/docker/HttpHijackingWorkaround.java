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
package edu.teco.smartlambda.container.docker;

import com.spotify.docker.client.LogReader;
import com.spotify.docker.client.LogStream;
import lombok.SneakyThrows;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.io.IdentityInputStream;
import org.apache.http.impl.io.SessionInputBufferImpl;
import org.glassfish.jersey.message.internal.EntityInputStream;
import sun.nio.ch.ChannelInputStream;

import java.io.FilterInputStream;
import java.io.OutputStream;
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
class HttpHijackingWorkaround {
	
	/**
	 * This is a utility class and shall not be instantiated
	 */
	private HttpHijackingWorkaround() {
		
	}
	
	/**
	 * Get a output stream that can be used to write into the standard input stream of  docker container's running process
	 *
	 * @param stream the docker container's log stream
	 * @param uri    the URI to the docker socket
	 *
	 * @return a writable byte channel that can be used to write into the http web-socket output stream
	 *
	 * @throws Exception on any docker or reflection exception
	 */
	static OutputStream getOutputStream(final LogStream stream, final String uri) throws Exception {
		// @formatter:off
		final String[] fields =
				new String[] {"reader",
				              "stream",
				              "original",
				              "input",
				              "in",
				              "in",
				              "in",
				              "eofWatcher",
				              "wrappedEntity",
				              "content",
				              "in",
				              "instream"};
		
		final String[] containingClasses =
				new String[] {"com.spotify.docker.client.DefaultLogStream",
				              LogReader.class.getName(),
				              "org.glassfish.jersey.message.internal.ReaderInterceptorExecutor$UnCloseableInputStream",
				              EntityInputStream.class.getName(),
				              FilterInputStream.class.getName(),
		                      FilterInputStream.class.getName(),
		                      FilterInputStream.class.getName(),
		                      EofSensorInputStream.class.getName(),
		                      HttpEntityWrapper.class.getName(),
		                      BasicHttpEntity.class.getName(),
		                      IdentityInputStream.class.getName(),
		                      SessionInputBufferImpl.class.getName()};
		// @formatter:on
		
		final List<String[]> fieldClassTuples = new LinkedList<>();
		for (int i = 0; i < fields.length; i++) {
			fieldClassTuples.add(new String[] {containingClasses[i], fields[i]});
		}
		
		if (uri.startsWith("unix:")) {
			fieldClassTuples.add(new String[] {ChannelInputStream.class.getName(), "ch"});
		} else if (uri.startsWith("https:")) {
			final float jvmVersion = Float.parseFloat(System.getProperty("java.specification.version"));
			fieldClassTuples.add(new String[] {"sun.security.ssl.AppInputStream", jvmVersion < 1.9f ? "c" : "socket"});
		} else {
			fieldClassTuples.add(new String[] {"java.net.SocketInputStream", "socket"});
		}
		
		final Object res = getInternalField(stream, fieldClassTuples);
		if (res instanceof WritableByteChannel) {
			return Channels.newOutputStream((WritableByteChannel) res);
		} else if (res instanceof Socket) {
			return ((Socket) res).getOutputStream();
		} else {
			throw new AssertionError("Expected " + WritableByteChannel.class.getName() + " or " + Socket.class.getName() + " but found: " +
					res.getClass().getName());
		}
	}
	
	/**
	 * Recursively traverse a hierarchy of fields in classes, obtain their value and continue the traversing on the optained object
	 *
	 * @param fieldContent     current object to operate on
	 * @param classFieldTupels the class/field hierarchy
	 *
	 * @return the content of the leaf in the traversed hierarchy path
	 */
	@SneakyThrows // since the expressions are constant the exceptions cannot occur (except when the library is changed, but then a crash
	// is favourable)
	private static Object getInternalField(final Object fieldContent, final List<String[]> classFieldTupels) {
		Object curr = fieldContent;
		for (final String[] classFieldTuple : classFieldTupels) {
			//noinspection ConstantConditions
			final Field field = Class.forName(classFieldTuple[0]).getDeclaredField(classFieldTuple[1]);
			field.setAccessible(true);
			curr = field.get(curr);
		}
		return curr;
	}
}

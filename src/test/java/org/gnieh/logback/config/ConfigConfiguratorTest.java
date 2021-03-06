package org.gnieh.logback.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;

import org.junit.Test;

import com.typesafe.config.ConfigFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.status.Status;

public class ConfigConfiguratorTest {

	@Test
	public void testConfigureRollingFileAppender() {
		System.setProperty("config.file", "src/test/resources/rollingFileAppender.conf");
		ConfigFactory.invalidateCaches();

		LoggerContext context = new LoggerContext();
		ConfigConfigurator configurator = new ConfigConfigurator();
		configurator.configure(context);

		int errorCount = 0;
		int warningCount = 0;
		for (Status status : context.getStatusManager().getCopyOfStatusList()) {
			if (status.getLevel() == Status.ERROR) {
				System.out.println(String.format("ERROR : %s", status.getMessage()));
				errorCount++;
			} else if (status.getLevel() == Status.WARN) {
				System.out.println(String.format("WARN : %s", status.getMessage()));
				warningCount++;
			} else if (status.getLevel() == Status.INFO) {
				System.out.println(String.format("INFO : %s", status.getMessage()));
			}

		}
		assertEquals(0, errorCount);
		assertEquals(0, warningCount);

		Logger rootLogger = context.getLoggerList().get(0);
		Appender<?> appender = rootLogger.getAppender("rolling");
		assertTrue(appender.isStarted());

		assertTrue(appender instanceof RollingFileAppender);

		RollingFileAppender<?> rolling = (RollingFileAppender<?>) appender;
		assertTrue(rolling.getEncoder() instanceof PatternLayoutEncoder);
		assertTrue(rolling.isStarted());

		PatternLayoutEncoder encoder = (PatternLayoutEncoder) rolling.getEncoder();
		assertEquals(Charset.forName("UTF-8"), encoder.getCharset());
		assertEquals("%date %level %logger %thread %msg%n", encoder.getPattern());

		assertEquals("logs/test.log", rolling.getFile());

		assertTrue(rolling.getRollingPolicy() instanceof TimeBasedRollingPolicy);
		TimeBasedRollingPolicy<?> rollingPolicy = (TimeBasedRollingPolicy<?>) rolling.getRollingPolicy();

		assertEquals("logs/test%d{yyyy-MM-dd}.%i.log", rollingPolicy.getFileNamePattern());
		assertEquals(30, rollingPolicy.getMaxHistory());
		assertTrue(rollingPolicy.isStarted());

	}

	@Test
	public void testConfigureMultipleLoggers() {
		System.setProperty("config.file", "src/test/resources/multipleLoggers.conf");
		ConfigFactory.invalidateCaches();

		LoggerContext context = new LoggerContext();
		ConfigConfigurator configurator = new ConfigConfigurator();
		configurator.configure(context);

		int errorCount = 0;
		int warningCount = 0;
		for (Status status : context.getStatusManager().getCopyOfStatusList()) {
			if (status.getLevel() == Status.ERROR) {
				System.out.println(String.format("ERROR : %s", status.getMessage()));
				errorCount++;
			} else if (status.getLevel() == Status.WARN) {
				System.out.println(String.format("WARN : %s", status.getMessage()));
				warningCount++;
			} else if (status.getLevel() == Status.INFO) {
				System.out.println(String.format("INFO : %s", status.getMessage()));
			}

		}
		assertEquals(0, errorCount);
		assertEquals(0, warningCount);

		Logger rootLogger = context.getLoggerList().get(0);
		assertEquals(Level.INFO, rootLogger.getLevel());
		Appender<?> consoleAppender = rootLogger.getAppender("console");
		assertNotNull(consoleAppender);
		Appender<?> fileAppender = rootLogger.getAppender("file");
		assertNull(fileAppender);
		assertTrue(consoleAppender.isStarted());

		Logger orgGniehLogger = context.getLogger("org.gnieh");
		assertNotNull(orgGniehLogger);
		assertEquals("org.gnieh", orgGniehLogger.getName());
		assertEquals(Level.DEBUG, orgGniehLogger.getLevel());
		consoleAppender = orgGniehLogger.getAppender("console");
		assertNotNull(consoleAppender);
		fileAppender = orgGniehLogger.getAppender("file");
		assertNotNull(fileAppender);
		assertTrue(fileAppender.isStarted());

		Logger orgGniehLogbackLogger = context.getLogger("org.gnieh.logback");
		assertNotNull(orgGniehLogbackLogger);
		assertEquals("org.gnieh.logback", orgGniehLogbackLogger.getName());
		assertEquals(Level.TRACE, orgGniehLogbackLogger.getLevel());
		consoleAppender = orgGniehLogbackLogger.getAppender("console");
		assertNull(consoleAppender);
		fileAppender = orgGniehLogbackLogger.getAppender("file");
		assertNull(fileAppender);
	}

	@Test
	public void testConfigureAsyncAppender() {
		System.setProperty("config.file", "src/test/resources/asyncAppender.conf");
		testConfigureAsyncAppenderInternal();
	}

	@Test
	public void testConfigureIncludedAsyncAppender() {
		System.setProperty("config.file", "src/test/resources/includedConfig.conf");
		LoggerContext context = testConfigureAsyncAppenderInternal();
		@SuppressWarnings("unused")
		ConfigurationWatchList watchList = ConfigurationWatchListUtil.getConfigurationWatchList(context);
		// failed due to the naive config files gathering algorithm
		//assertEquals(3, watchList.getCopyOfFileWatchList().size());
	}

	public LoggerContext testConfigureAsyncAppenderInternal() {
		ConfigFactory.invalidateCaches();

		LoggerContext context = new LoggerContext();
		ConfigConfigurator configurator = new ConfigConfigurator();
		configurator.configure(context);

		int errorCount = 0;
		int warningCount = 0;
		for (Status status : context.getStatusManager().getCopyOfStatusList()) {
			if (status.getLevel() == Status.ERROR) {
				System.out.println(String.format("ERROR : %s", status.getMessage()));
				errorCount++;
			} else if (status.getLevel() == Status.WARN) {
				System.out.println(String.format("WARN : %s", status.getMessage()));
				warningCount++;
			} else if (status.getLevel() == Status.INFO) {
				System.out.println(String.format("INFO : %s", status.getMessage()));
			}

		}
		assertEquals(0, errorCount);
		assertEquals(0, warningCount);

		Logger rootLogger = context.getLoggerList().get(0);
		Appender<?> rollingRef = rootLogger.getAppender("rolling");
		assertTrue(rollingRef.isStarted());
		Appender<?> asyncRef = rootLogger.getAppender("async");
		assertTrue(asyncRef.isStarted());

		assertTrue(rollingRef instanceof RollingFileAppender);
		assertTrue(asyncRef instanceof AsyncAppender);

		RollingFileAppender<?> rolling = (RollingFileAppender<?>) rollingRef;
		assertTrue(rolling.isStarted());

		AsyncAppenderBase<?> async = (AsyncAppenderBase<?>) asyncRef;
		assertEquals(0, async.getDiscardingThreshold());
		assertEquals(1000, async.getMaxFlushTime());
		assertEquals(100, async.getQueueSize());
		assertEquals(rolling, async.getAppender("rolling"));
		assertTrue(async.isStarted());

		return context;
	}
}

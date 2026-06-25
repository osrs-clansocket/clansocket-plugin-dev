package com.clansocket.observation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

final class JfrSmokeTest
{
	private final int spinIterations = 1000;
	private final int recordingDurationSeconds = 2;

	@Test
	void jfrRecordingCapturesEvents() throws IOException
	{
		final Path traceFile = Files.createTempFile("clansocket-jfr-smoke-", ".jfr");
		try (Recording recording = new Recording())
		{
			recording.enable("jdk.JavaMonitorEnter");
			recording.enable("jdk.ThreadAllocationStatistics");
			recording.setDuration(Duration.ofSeconds(recordingDurationSeconds));
			recording.start();
			doSomeWork();
			recording.dump(traceFile);
		}
		final int eventCount = countEvents(traceFile);
		Files.deleteIfExists(traceFile);
		assertThat(eventCount).isPositive();
	}

	private void doSomeWork()
	{
		long acc = 0;
		for (int i = 0; i < spinIterations; i++)
		{
			acc += i;
		}
		assertThat(acc).isNotZero();
	}

	private static int countEvents(final Path traceFile) throws IOException
	{
		int count = 0;
		try (RecordingFile rf = new RecordingFile(traceFile))
		{
			while (rf.hasMoreEvents())
			{
				final RecordedEvent event = rf.readEvent();
				if (event != null)
				{
					count++;
				}
			}
		}
		return count;
	}
}

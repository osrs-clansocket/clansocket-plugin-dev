package com.clansocket.panel.verify;

import java.awt.Color;

public final class VerifyConstants
{
	public static final int BORDER_WIDTH = 1;
	public static final int PULSE_INTERVAL_MS = 50;
	public static final float PULSE_STEP = 0.02f;
	public static final int GAP = 2;
	public static final int SUBTITLE_WIDTH = 180;
	public static final long MS_PER_MINUTE = 60_000L;
	public static final long MS_PER_SECOND = 1_000L;
	public static final Color PULSE_GREEN = new Color(70, 200, 110);
	public static final Color PULSE_GRAY = new Color(80, 80, 80);
	public static final Color BTN_REJECT = new Color(180, 70, 70);

	public static final String LABEL_CONFIRM = "CONFIRM";
	public static final String LABEL_REJECT = "REJECT";
	public static final String REJECT_CONFIRM_TITLE = "ClanSocket";

	public static final String LABEL_TITLE_RSN = "rsn verify request";
	public static final String REJECT_CONFIRM_MESSAGE_RSN = "Reject this rsn verification? The requester will need to submit a new verify request on clansocket.com.";
	public static final String FMT_SUBTITLE_RSN = "<html><body style='width:%1$dpx'>%2$s wants to verify rsn '<b>%3$s</b>' on clansocket.com</body></html>";

	public static final String LABEL_TITLE_CLAIM = "clan claim request";
	public static final String REJECT_CONFIRM_MESSAGE_CLAIM = "Reject this clan claim? The requester will not be granted ownership.";
	public static final String FMT_SUBTITLE_CLAIM = "<html><body style='width:%1$dpx'>%2$s wants to claim clan '<b>%4$s</b>' as rsn '<b>%3$s</b>' on clansocket.com</body></html>";

	public static final String FMT_COUNTDOWN = "%dm %ds left";

	private VerifyConstants() {
	}

	public static Color pulseColor(final float phase)
	{
		final double t = 0.5 + 0.5 * Math.sin(phase * 2.0 * Math.PI);
		final int r = (int) (PULSE_GREEN.getRed() * t + PULSE_GRAY.getRed() * (1.0 - t));
		final int g = (int) (PULSE_GREEN.getGreen() * t + PULSE_GRAY.getGreen() * (1.0 - t));
		final int b = (int) (PULSE_GREEN.getBlue() * t + PULSE_GRAY.getBlue() * (1.0 - t));
		return new Color(r, g, b);
	}
}

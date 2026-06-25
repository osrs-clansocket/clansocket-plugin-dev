package com.clansocket.bus.primitive;

public abstract class ArmedState
{
	private boolean armed;

	public final boolean isArmed()
	{
		return armed;
	}

	public final void arm()
	{
		armed = true;
	}

	public final void disarm()
	{
		armed = false;
	}

	public abstract void clear();
}

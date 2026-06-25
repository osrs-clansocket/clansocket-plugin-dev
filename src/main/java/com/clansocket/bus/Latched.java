package com.clansocket.bus;

import java.util.Objects;

public final class Latched<T>
{
	private T last;
	private boolean initialized;

	public Latched() {
	}

	public Latched(final T initial) {
		this.last = initial;
		this.initialized = true;
	}

	public boolean update(final T next)
	{
		if (initialized && Objects.equals(last, next))
		{
			return false;
		}
		last = next;
		initialized = true;
		return true;
	}

	public void preset(final T value)
	{
		last = value;
		initialized = true;
	}

	public void reset()
	{
		last = null;
		initialized = false;
	}

	public T current()
	{
		return last;
	}
}

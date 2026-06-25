package com.clansocket.world;

final class WorldConstants
{
	static final String LOCATIONS_RESOURCE = "locations.json";
	static final int CHUNK_PAIR_SIZE = 2;
	static final int RX_SHIFT = 8;
	static final int RY_MASK = 0xFF;
	static final int PACK_SHIFT = 32;
	static final long PACK_MASK = 0xFFFFFFFFL;

	private WorldConstants() {
	}
}

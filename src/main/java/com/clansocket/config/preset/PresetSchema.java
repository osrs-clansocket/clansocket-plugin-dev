package com.clansocket.config.preset;

import java.util.Map;

import com.google.gson.JsonElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresetSchema
{
	private int version;
	private Map<String, JsonElement> values;
}

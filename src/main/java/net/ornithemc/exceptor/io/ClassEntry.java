package net.ornithemc.exceptor.io;

import java.util.LinkedHashMap;
import java.util.Map;

public record ClassEntry(String name, Map<String, MethodEntry> methods) {

	public ClassEntry(String name) {
		this(name, new LinkedHashMap<>());
	}
}

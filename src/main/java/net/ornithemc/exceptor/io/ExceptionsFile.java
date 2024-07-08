package net.ornithemc.exceptor.io;

import java.util.LinkedHashMap;
import java.util.Map;

public record ExceptionsFile(Map<String, ClassEntry> classes) {

	public ExceptionsFile() {
		this(new LinkedHashMap<>());
	}
}

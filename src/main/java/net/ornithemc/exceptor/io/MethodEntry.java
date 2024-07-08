package net.ornithemc.exceptor.io;

import java.util.ArrayList;
import java.util.List;

public record MethodEntry(String name, String descriptor, List<String> exceptions) {

	public MethodEntry(String name, String descriptor) {
		this(name, descriptor, new ArrayList<>());
	}
}

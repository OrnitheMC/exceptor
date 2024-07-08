package net.ornithemc.exceptor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

	public static void main(String[] args) {
		if (args.length == 0) {
			printHelpAndThrow("no command given!");
		}

		String command = args[0];

		switch (command) {
			case "help"    -> printHelp();
			case "apply"   -> apply(args);
			case "extract" -> extract(args);
			default        -> printHelpAndThrow("unknown command: " + command);
		}
	}

	private static void apply(String[] args) {
		if (args.length != 3) {
			printHelpAndThrow("[apply] expected 2 arguments, got " + args.length);
		}

		Path jar = Paths.get(args[1]);
		Path exceptions = Paths.get(args[2]);

		try {
			Exceptor.apply(jar, exceptions);
		} catch (IOException e) {
			throw new RuntimeException("error while applying exceptions", e);
		}
	}

	private static void extract(String[] args) {
		if (args.length != 3) {
			printHelpAndThrow("[extract] expected 2 arguments, got " + args.length);
		}

		Path jar = Paths.get(args[1]);
		Path exceptions = Paths.get(args[2]);

		try {
			Exceptor.extract(jar, exceptions);
		} catch (IOException e) {
			throw new RuntimeException("error while extracting exceptions", e);
		}
	}

	private static void printHelp() {
		System.out.println("=== EXCEPTOR HELP ===");
		System.out.println("commands:");
		System.out.println("  help: print available commands");
		System.out.println("  apply <jar> <exceptions>: patch a jar with exceptions");
		System.out.println("    <jar>: path to the jar file");
		System.out.println("    <exceptions>: path to the exceptions file");
		System.out.println("  extract <jar> <exceptions>: extract exceptions from a jar");
		System.out.println("    <jar>: path to the jar file");
		System.out.println("    <exceptions>: path to the exceptions file");
	}

	private static void printHelpAndThrow(String message) {
		printHelp();
		throw new RuntimeException(message);
	}
}

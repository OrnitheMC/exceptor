package net.ornithemc.exceptor.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExceptorIo {

	private static final String TAB = "\t";

	public static ExceptionsFile read(Path path) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(path)) {
			return read(br);
		}
	}

	public static ExceptionsFile read(BufferedReader br) throws IOException {
		ExceptionsFile exceptions = new ExceptionsFile();
		read(br, exceptions);
		return exceptions;
	}

	public static void read(Path path, ExceptionsFile exceptions) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(path)) {
			read(br, exceptions);
		}
	}

	public static void read(BufferedReader br, ExceptionsFile exceptions) throws IOException {
		int lineNumber = 0;

		try {
			String line = null;
			ClassEntry cls = null;

			while ((line = br.readLine()) != null) {
				lineNumber++;

				if (line.isBlank() || line.startsWith("#")) {
					continue;
				}

				String[] args = line.split(TAB);
				int indents = 0;

				for (; indents < args.length; indents++) {
					if (!args[indents].isEmpty()) {
						break;
					}
				}

				switch (indents) {
				case 0:
					if (args.length > 1) {
						throw new IOException("too many arguments (" + args.length + ") for class entry!");
					}

					cls = new ClassEntry(args[0]);
					exceptions.classes().put(cls.name(), cls);

					break;
				case 1:
					if (cls == null) {
						throw new IOException("missing class entry for method entry!");
					}
					if (args.length < 4) {
						throw new IOException("too few arguments (" + args.length + ") for method entry!");
					}

					MethodEntry mtd = cls.methods().get(args[1] + args[2]);

					if (mtd == null) {
						mtd = new MethodEntry(args[1], args[2]);
						cls.methods().put(args[1] + args[2], mtd);
					}

					for (int i = 3; i < args.length; i++) {
						mtd.exceptions().add(args[i]);
					}

					break;
				default:
					throw new IOException("invalid number of indents (" + indents + ")");
				}
			}
		} catch (Throwable t) {
			throw new IOException("badly formatted file on line " + lineNumber);
		}
	}

	public static void write(Path path, ExceptionsFile exceptions) throws IOException {
		try (BufferedWriter bw = Files.newBufferedWriter(path)) {
			write(bw, exceptions);
		}
	}

	public static void write(BufferedWriter bw, ExceptionsFile exceptions) throws IOException {
		for (ClassEntry cls : exceptions.classes().values()) {
			bw.write(cls.name());
			bw.newLine();

			for (MethodEntry mtd : cls.methods().values()) {
				bw.write(TAB);
				bw.write(mtd.name());
				bw.write(TAB);
				bw.write(mtd.descriptor());
				for (String exception : mtd.exceptions()) {
					bw.write(TAB);
					bw.write(exception);
				}
				bw.newLine();
			}
		}
	}
}

package net.ornithemc.exceptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.ornithemc.exceptor.io.ClassEntry;
import net.ornithemc.exceptor.io.ExceptionsFile;
import net.ornithemc.exceptor.io.ExceptorIo;
import net.ornithemc.exceptor.io.MethodEntry;

public class Exceptor {

	public static void apply(Path jar, Path exceptions) throws IOException {
		Exceptor.apply(jar, ExceptorIo.read(exceptions));
	}

	public static void apply(Path jar, ExceptionsFile exceptions) throws IOException {
		Exceptor.iterateClasses(jar, (classFile, classReader) -> {
			String className = classReader.getClassName();
			ClassEntry cls = exceptions.classes().get(className);

			if (cls == null) {
				return;
			}

			ClassWriter writer = new ClassWriter(classReader, Opcodes.ASM9);
			ClassVisitor patcher = new ClassVisitor(Opcodes.ASM9, writer) {

				@Override
				public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
					MethodEntry mtd = cls.methods().get(name + descriptor);

					if (mtd != null) {
						if (mtd.exceptions().isEmpty()) {
							exceptions = null;
						} else {
							exceptions = mtd.exceptions().toArray(String[]::new);
						}
					}

					return super.visitMethod(access, name, descriptor, signature, exceptions);
				}
			};

			classReader.accept(patcher, 0);

			try {
				Files.write(classFile, writer.toByteArray());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	public static void extract(Path jar, Path exceptions) throws IOException {
		ExceptionsFile file = new ExceptionsFile();
		Exceptor.extract(jar, file);
		ExceptorIo.write(exceptions, file);
	}

	public static void extract(Path jar, ExceptionsFile exceptions) throws IOException {
		Exceptor.iterateClasses(jar, (classFile, classReader) -> {
			ClassVisitor extractor = new ClassVisitor(Opcodes.ASM9) {

				private ClassEntry cls;

				@Override
				public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
					this.cls = exceptions.classes().get(name);

					if (this.cls == null) {
						this.cls = new ClassEntry(name);
					}

					super.visit(version, access, name, signature, superName, interfaces);
				}

				@Override
				public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
					if (exceptions != null && exceptions.length > 0) {
						MethodEntry mtd = this.cls.methods().get(name + descriptor);

						if (mtd == null) {
							mtd = new MethodEntry(name, descriptor);
						}

						mtd.exceptions().addAll(Arrays.asList(exceptions));
						this.cls.methods().putIfAbsent(name + descriptor, mtd);
					}

					return super.visitMethod(access, name, descriptor, signature, exceptions);
				}

				@Override
				public void visitEnd() {
					if (!this.cls.methods().isEmpty()) {
						exceptions.classes().putIfAbsent(this.cls.name(), this.cls);
					}

					super.visitEnd();
				}
			};

			classReader.accept(extractor, 0);
		});
	}

	private static void iterateClasses(Path jar, BiConsumer<Path, ClassReader> action) throws IOException {
		try (FileSystem fs = FileSystems.newFileSystem(jar)) {
			for (Path root : fs.getRootDirectories()) {
				try (Stream<Path> classFiles = Files.find(root, Integer.MAX_VALUE, (p, a) -> a.isRegularFile() && p.toString().endsWith(".class"))) {
					classFiles.forEach(classFile -> {
						try (InputStream is = Files.newInputStream(classFile)) {
							action.accept(classFile, new ClassReader(is));
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
				} catch (UncheckedIOException e) {
					throw e.getCause();
				}
			}
		}
	}
}

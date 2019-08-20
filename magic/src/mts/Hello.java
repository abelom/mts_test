package mts;

import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class Hello {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        loadAndScanJar(new File("reality/mts_test.jar"));
    }

    public static void loadAndScanJar(File jarFile)
            throws ClassNotFoundException, ZipException, IOException {

        JarFile jar = new JarFile(jarFile);

        // Getting the files into the jar
        Enumeration<? extends JarEntry> enumeration = jar.entries();


        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();

            // Is this a class?
            if (zipEntry.getName().endsWith(".class")) {
                System.out.println("ClassFile " + zipEntry.getName());

                String className = zipEntry.getName().replace(".class", "").replace("/", ".");
                System.out.println("Class: " + className);

                InputStream classContent = jar.getInputStream(zipEntry);
                playWith(classContent);
                classContent.close();


            }
        }

    }

    private static void playWith(InputStream classContent) throws IOException {
        class MethodAnnotationScanner extends MethodVisitor {
            public MethodAnnotationScanner() {
                super(Opcodes.ASM5);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                System.out.println("MethodAnnotationScanner visitAnnotation: desc=" + desc + " visible=" + visible);
                return super.visitAnnotation(desc, visible);
            }
        }

        class FieldAnnotationScanner extends FieldVisitor {
            public FieldAnnotationScanner() {
                super(Opcodes.ASM5);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                System.out.println("FieldAnnotationScanner visitAnnotation: desc=" + desc + " visible=" + visible);
                return super.visitAnnotation(desc, visible);
            }
        }

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM5) {
            /**
             * Called when a class is visited. This is the method called first
             */
            @Override
            public void visit(int version, int access, String name,
                              String signature, String superName, String[] interfaces) {
                System.out.println("Visiting class: " + name);
                System.out.println("Class Major Version: " + version);
                System.out.println("Super class: " + superName);
                super.visit(version, access, name, signature, superName, interfaces);
            }

            /**
             * When a method is encountered
             */
            @Override
            public MethodVisitor visitMethod(int access, String name,
                                             String desc, String signature, String[] exceptions) {
                System.out.println("Method: " + name + " " + desc);
                return new MethodAnnotationScanner();
            }


            /**
             * When a field is encountered
             */
            @Override
            public FieldVisitor visitField(int access, String name,
                                           String desc, String signature, Object value) {
                System.out.println("Field: " + name + " " + desc + " value:" + value);
                return new FieldAnnotationScanner();
            }


            @Override
            public void visitEnd() {
                System.out.println("Method ends here");
                super.visitEnd();
            }


        };

        ClassReader classReader = new ClassReader(classContent);
        classReader.accept(visitor, 0);

    }
}

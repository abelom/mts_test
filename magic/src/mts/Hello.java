package mts;

import org.objectweb.asm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class Hello {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        Map<String, ClassMeta> info = new TreeMap<>();


        loadAndScanJar(new File("reality/mts_test.jar"), info);
    }

    public static void loadAndScanJar(File jarFile, Map<String, ClassMeta> info)
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
                playWith(classContent, info);
                classContent.close();
            }
        }

    }

    private static void playWith(InputStream classContent, Map<String, ClassMeta> info) throws IOException {
        ClassMeta meta = new ClassMeta();


        class MethodAnnotationScanner extends MethodVisitor {
            public MethodAnnotationScanner() {
                super(Opcodes.ASM5);
            }

            @Override
            public void visitFieldInsn(int opcode,
                                       String owner,
                                       String name,
                                       String desc) {
                super.visitFieldInsn(opcode, owner, name, desc);
                System.out.println(meta.getName() + ": getField " + name + " owner=" + owner + " " + desc);
            }

            @Override
            public void visitTypeInsn(int i, String s) {
                super.visitTypeInsn(i, s);
                System.out.println("visitTypeInsn " + s);
            }

            @Override
            public void visitMethodInsn(int opcode,
                                        String owner,
                                        String name,
                                        String desc,
                                        boolean itf) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);

                System.out.println(meta.getName() + ": invokeMethod " + opcode + " owner=" + owner + ", name " + name + " " + desc);
                System.out.println();
            }

            @Override
            public void visitInvokeDynamicInsn(String s, String s1, Handle handle, Object... objects) {
                super.visitInvokeDynamicInsn(s, s1, handle, objects);
                System.out.println("visitInvokeDynamicInsn" + s + s1);
            }

            @Override
            public void visitLocalVariable(String s, String s1, String s2, Label label, Label label1, int i) {
                super.visitLocalVariable(s, s1, s2, label, label1, i);
                System.out.println("visitLocalVariable " + s + s1 + s2);
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
                if (desc.contains(Autowired.class.getSimpleName()) || desc.contains(Value.class.getSimpleName()))
                    meta.setSpringBean(true);
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
                meta.setName(name);
                meta.setSuperName(superName);

                info.putIfAbsent(name, meta);

                super.visit(version, access, name, signature, superName, interfaces);
            }

            /**
             * When a method is encountered
             */
            @Override
            public MethodVisitor visitMethod(int access, String name,
                                             String desc, String signature, String[] exceptions) {
                System.out.println("Declare Method: " + name + " signature=" + desc);

                if (name.equals("getInstance"))
                    meta.setSingleton(true);

                return new MethodAnnotationScanner();
            }


            /**
             * When a field is encountered
             */
            @Override
            public FieldVisitor visitField(int access, String name,
                                           String desc, String signature, Object value) {
                System.out.println(meta.getName() + " DeclareField: " + name + " " + desc + " value:" + value);
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

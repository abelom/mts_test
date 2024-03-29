package mts;

import org.objectweb.asm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class Hello {
    private static boolean isDebugEnabled = Boolean.getBoolean("mts.debug");
    private static int classes;


    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String fileName = args[0];
        info("Hello " + fileName);

        State state = new State();
        loadAndScanJar(new File(fileName), state);

        state.runMagic();

        FileWriter fw = new FileWriter("hints.txt");
        fw.write(state.hints.toString());
        fw.close();


        info("Found total " + classes + " classes");
        info("Maybe " + state.singletons.size() + " total singletons");
    }

    public static void loadAndScanJar(File jarFile, State state)
            throws ClassNotFoundException, ZipException, IOException {

        JarFile jar = new JarFile(jarFile);

        // Getting the files into the jar
        Enumeration<? extends JarEntry> enumeration = jar.entries();

        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();

            // Is this a class?
            if (zipEntry.getName().endsWith(".class")) {
                if (isDebugEnabled())
                    debug("ClassFile " + zipEntry.getName());

                String className = zipEntry.getName().replace(".class", "").replace("/", ".");
                debug("Class: " + className);

                classes++;

                InputStream classContent = jar.getInputStream(zipEntry);
                playWith(classContent, state);
                classContent.close();
            }
        }

    }

    private static void playWith(InputStream classContent, State state) throws IOException {
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
                if (isDebugEnabled())
                    System.out.println(meta.getName() + ": getField " + name + " owner=" + owner + " " + desc);
            }

            @Override
            public void visitTypeInsn(int i, String s) {
                super.visitTypeInsn(i, s);
                if (isDebugEnabled())
                    System.out.println("visitTypeInsn " + s);
            }

            @Override
            public void visitMethodInsn(int opcode,
                                        String owner,
                                        String name,
                                        String desc,
                                        boolean itf) {
                /*
                 * We are here when we invoke a method
                 */

                super.visitMethodInsn(opcode, owner, name, desc, itf);

                if (isDebugEnabled())
                    System.out.println(meta.getName() + ": invokeMethod " + opcode + " owner=" + owner + ", name " + name + " " + desc);
                MethodReference methodKey = new MethodReference(owner, name);
                state.methodUsages.putIfAbsent(methodKey, new ArrayList<>());
                state.methodUsages.get(methodKey).add(meta.getName());
            }

            @Override
            public void visitInvokeDynamicInsn(String s, String s1, Handle handle, Object... objects) {
                super.visitInvokeDynamicInsn(s, s1, handle, objects);
                if (isDebugEnabled())
                    System.out.println("visitInvokeDynamicInsn" + s + s1);
            }

            @Override
            public void visitLocalVariable(String s, String s1, String s2, Label label, Label label1, int i) {
                super.visitLocalVariable(s, s1, s2, label, label1, i);
                if (isDebugEnabled())
                    System.out.println("visitLocalVariable " + s + s1 + s2);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (isDebugEnabled()) {
                    System.out.println("MethodAnnotationScanner visitAnnotation: desc=" + desc + " visible=" + visible);
                }
                return super.visitAnnotation(desc, visible);
            }
        }

        class FieldAnnotationScanner extends FieldVisitor {
            public FieldAnnotationScanner() {
                super(Opcodes.ASM5);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (isDebugEnabled())
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
                if (isDebugEnabled()) {
                    debug("Visiting class: " + name);
                    System.out.println("Class Major Version: " + version);
                    System.out.println("Super class: " + superName);
                }
                meta.setName(name);
                meta.setSuperName(superName);

                state.info.put(name, meta);

                super.visit(version, access, name, signature, superName, interfaces);
            }

            /**
             * When a method is encountered
             */
            @Override
            public MethodVisitor visitMethod(int access, String name,
                                             String desc, String signature, String[] exceptions) {
                if (isDebugEnabled())
                    System.out.println("Declare Method: " + name + " signature=" + desc);

                if (name.equals(MagicConstants.GET_INSTANCE) && !meta.getName().toLowerCase().endsWith(MagicConstants.DAO_SUFFIX)) {
                    info("FOUND_SINGLETON: " + meta.getName());
                    state.singletons.add(meta);
                    meta.setSingleton(true);
                }

                return new MethodAnnotationScanner();
            }


            /**
             * When a field is encountered
             */
            @Override
            public FieldVisitor visitField(int access, String name,
                                           String desc, String signature, Object value) {
                if (isDebugEnabled())
                    System.out.println(meta.getName() + " DeclareField: " + name + " " + desc + " value:" + value);
                return new FieldAnnotationScanner();
            }


            @Override
            public void visitEnd() {
                if (isDebugEnabled())
                    debug("Method ends here");
                super.visitEnd();
            }
        };

        ClassReader classReader = new ClassReader(classContent);
        classReader.accept(visitor, 0);

    }

    static boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    static void debug(String line) {
        if (!isDebugEnabled())
            return;
        System.out.println(line);
    }

    static void info(String line) {
        System.out.println(new Date() + ": " + line);
    }
}

package mts;

import java.util.*;

public class State {
    public Map<String, ClassMeta> info = new TreeMap<>();
    public Set<ClassMeta> singletons = new HashSet<>();

    protected StringBuilder hints = new StringBuilder();

    Map<MethodReference, List<String>> methodUsages = new HashMap<>();

    public void runMagic() {
        for (ClassMeta singletonClass : singletons) {

            findUsages(singletonClass);


        }
    }

    private void findUsages(ClassMeta singletonClass) {
        List<String> usages = findMethodUsages(singletonClass.getName(), MagicConstants.GET_INSTANCE);

        System.out.println("Found " + singletonClass + ". Total usages: " + usages.size());

        hints.append(singletonClass + " usages:\n");

        boolean hasNotGreatUsages = false;
        for (String usage : usages) {
            ClassMeta meta = Objects.requireNonNull(info.get(usage), "meta");

            if (!meta.isSpringBean()) {
                System.out.printf(singletonClass + " not great usage " + usage);
                hasNotGreatUsages = true;
                hints.append(singletonClass + " not great usage " + usage + "\n");
            }
        }


        if (!hasNotGreatUsages) {
            Hello.info("HAPPY Spring " + singletonClass);
        }
        helpWithImplementation(singletonClass);


    }

    private void helpWithImplementation(ClassMeta meta) {
        hints.append("Regarding " + meta.getName() + "\r\n");
        String clazz = getShortName(meta.getName());
        String variableName = lowerCaseFirstChar(clazz);
        hints.append("\t@Autowired\n\tprivate " + clazz + " " + variableName + ";\n\n");

        hints.append("\t@Bean\n\tpublic " + clazz + " get" + clazz + "() {\n");
        hints.append("\t\treturn new " + clazz + "();\n");
        hints.append("\t}\n\n");
    }

    private String lowerCaseFirstChar(String clazz) {
        return clazz.substring(0, 1).toLowerCase() + clazz.substring(1);
    }

    private String getShortName(String fullClassName) {
        int index = fullClassName.lastIndexOf('/');
        return fullClassName.substring(index + 1);
    }

    private List<String> findMethodUsages(String clazz, String methodName) {
        for (Map.Entry<MethodReference, List<String>> e : methodUsages.entrySet()) {
            if (!e.getKey().getMethodName().equals(methodName)) {
                // todo: search for any method, add a search queue
                continue;
            }

            if (!e.getKey().getClazz().equals(clazz))
                continue;
            return e.getValue();
        }
        return Collections.emptyList();
    }
}

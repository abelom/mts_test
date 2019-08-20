package mts;

import java.util.Objects;

public class MethodReference {
    private final String clazz;
    private final String methodName;

    public MethodReference(String clazz, String methodName) {
        this.clazz = Objects.requireNonNull(clazz);
        this.methodName = Objects.requireNonNull(methodName);
    }

    public String getClazz() {
        return clazz;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodReference that = (MethodReference) o;
        return clazz.equals(that.clazz) &&
                methodName.equals(that.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, methodName);
    }

    @Override
    public String toString() {
        return "MethodReference{" +
                "clazz='" + clazz + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}

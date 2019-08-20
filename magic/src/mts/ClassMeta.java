package mts;

public class ClassMeta {
    private String name;
    private String superName;
    private boolean isSingleton;
    private boolean isSpringBean;

    public void setName(String name) {
        this.name = name;
        if (name.endsWith(MagicConstants.TEST_SUFFIX)) {
            // todo: rename this field?
            isSpringBean = true;
        }
    }

    public String getName() {
        return name;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public String getSuperName() {
        return superName;
    }

    public boolean isSingleton() {
        return isSingleton;
    }

    public void setSingleton(boolean isSingleton) {
        this.isSingleton = isSingleton;
    }

    public boolean isSpringBean() {
        return isSpringBean;
    }

    public void setSpringBean(boolean springBean) {
        this.isSpringBean = springBean;
    }

    @Override
    public String toString() {
        return "ClassMeta{" +
                "name='" + name + '\'' +
                '}';
    }
}

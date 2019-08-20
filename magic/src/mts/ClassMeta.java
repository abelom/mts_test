package mts;

public class ClassMeta {
    private String name;
    private String superName;
    private boolean isSingleton;
    private boolean springBean;

    public void setName(String name) {
        this.name = name;
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
        return springBean;
    }

    public void setSpringBean(boolean springBean) {
        this.springBean = springBean;
    }
}

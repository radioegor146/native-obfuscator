package pack.tests.reflects.loader;

public class LRun {
    public void run() throws Exception {
        Loader l = new Loader();
        Class<?> c = l.findClass("pack.tests.reflects.loader.LTest");
        Object o = c.newInstance();
        c.getMethod("run").invoke(o);
    }
}

package pack;

import pack.tests.basics.accu.Digi;
import pack.tests.basics.cross.Top;
import pack.tests.basics.ctrl.Ctrl;
import pack.tests.basics.inner.Test;
import pack.tests.basics.overwirte.Sub;
import pack.tests.basics.runable.Task;
import pack.tests.basics.sub.Solver;
import pack.tests.bench.Calc;
import pack.tests.reflects.annot.annot;
import pack.tests.reflects.counter.Count;
import pack.tests.reflects.field.FTest;
import pack.tests.reflects.loader.LRun;
import pack.tests.reflects.res.Accesor;
import pack.tests.reflects.retrace.Tracer;
import pack.tests.security.SecTest;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Obfuscator Test Program");
        System.out.println("Author: huzpsb");
        System.out.println("Version: 1.0r");
        System.out.println("Link: https://github.com/huzpsb/JavaObfuscatorTest");
        System.out.println("-------------Test #1: Basics-------------");
        System.out.print("Test 1.1: Inheritance ");
        try {
            new Sub().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.print("Test 1.2: Cross ");
        try {
            new Top().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.print("Test 1.3: Throw ");
        try {
            new Ctrl().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.print("Test 1.4: Accuracy ");
        try {
            new Digi().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.print("Test 1.5: SubClass ");
        try {
            new Solver();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.print("Test 1.6: Pool ");
        try {
            // new Task().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.print("Test 1.7: InnerClass ");
        try {
            new Test().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.println("-------------Test #2: Reflects-------------");
        System.out.print("Test 2.1: Counter ");
        try {
            new Count().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.print("Test 2.2: Chinese 通过LMAO\b\b\b\b    \n");
        System.out.print("Test 2.3: Resource ");
        try {
            new Accesor().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.print("Test 2.4: Field ");
        try {
            new FTest().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.print("Test 2.5: Loader ");
        try {
            new LRun().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.print("Test 2.6: ReTrace ");
        try {
            new Tracer().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.print("Test 2.7: Annotation ");
        try {
            new annot().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.print("Test 2.8: Sec ");
        try {
            new SecTest().run();
        } catch (Throwable t) {
            System.out.println("ERROR");
        }
        System.out.println("-------------Test #3: Efficiency-------------");
        Calc.runAll();
        System.out.println("-------------Tests r Finished-------------");
    }
}

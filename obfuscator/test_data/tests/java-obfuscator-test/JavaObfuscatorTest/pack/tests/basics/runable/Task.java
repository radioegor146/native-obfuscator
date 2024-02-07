package pack.tests.basics.runable;

import java.util.concurrent.RejectedExecutionException;

public class Task {
    public void run() throws Exception {
        Exec e1 = new Exec(2);
        Exec e2 = new Exec(3);
        Exec e3 = new Exec(100);
        try {
            Pool.tpe.submit(e2::doAdd);
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
            }
            Pool.tpe.submit(() -> {
                int ix = Exec.i;
                e1.doAdd();
                Exec.i += ix;
            });
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
            }
            Pool.tpe.submit(e3::doAdd);
        } catch (RejectedExecutionException e) {
            Exec.i += 10;
        }
        Thread.sleep(300L);
        if (Exec.i == 30) {
            // 1->4(+3,e2)->14(+10,catch)->16(+2,e1)->30(+14,ix)
            System.out.println("PASS");
        } else {
            System.out.println("FAIL: " + Exec.i);
        }
    }
}

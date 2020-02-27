import java.util.LinkedList;
import java.util.Queue;

abstract class Monitor {

    Queue<Thread> e;
    Queue<Thread> s;
    Queue<Thread> w;

    Thread current;

    Monitor() {
        this.e = new LinkedList<>();
        this.s = new LinkedList<>();
        this.w = new LinkedList<>();
        this.current = null;
    }

    abstract void enter(Thread thread);

    abstract void exit();

    abstract void await() throws InterruptedException;

    abstract void signal();

}

class MonitorSC extends Monitor {

    public MonitorSC() {
        super();
    }

    @Override
    void enter(Thread thread) {
        if (this.current == null) {
            this.current = thread;
        } else {
            this.e.offer(thread);
        }
    }

    @Override
    void exit() {
        this.schedule();
    }

    @Override
    void await() throws InterruptedException {
        this.w.offer(this.current);
        this.current.wait();
        this.schedule();
    }

    @Override
    void signal() {
        if (this.w.isEmpty())
            return;

        Thread t = this.w.poll();
        t.start();
        this.e.offer(t);
    }

    private void schedule() {
        if (this.e.isEmpty()) {
            this.current = null;
        } else {
            this.current = this.e.poll();
        }
    }

}

class MonitorSW extends Monitor {

    public MonitorSW() {
        super();
    }

    @Override
    void enter(Thread thread) {
        if (this.current == null) {
            this.current = thread;
        } else {
            this.e.offer(thread);
        }
    }

    @Override
    void exit() {
        this.schedule();
    }

    @Override
    void await() throws InterruptedException {
        this.w.offer(this.current);
        this.current.wait();
        this.schedule();
    }

    @Override
    void signal() {
        if (this.w.isEmpty())
            return;

        Thread t = this.w.poll();
        t.start();
        this.s.offer(t);
        this.e.offer(this.current);
        schedule();
    }

    private void schedule() {
        if (!s.isEmpty()) {
            this.current = s.poll();
        } else if (!e.isEmpty()) {
            this.current = e.poll();
        } else {
            this.current = null;
        }
    }
}

class MonitorSUW extends Monitor {

    Thread next;

    public MonitorSUW() {
        super();
        next = null;
    }

    @Override
    void enter(Thread thread) {
        if (this.current == null) {
            this.current = thread;
        } else {
            this.e.offer(thread);
        }
    }

    @Override
    void exit() {
        this.schedule();
    }

    @Override
    void await() throws InterruptedException {
        this.w.offer(this.current);
        this.current.wait();
        this.schedule();
    }

    @Override
    void signal() {
        if (this.w.isEmpty())
            return;

        Thread t = this.w.poll();
        t.start();
        this.next = this.current;
        this.current = t;
    }

    private void schedule() {
        if (next != null) {
            this.current = next;
            this.current = null;
        } else if (!s.isEmpty()) {
            this.current = s.poll();
        } else if (!e.isEmpty()) {
            this.current = e.poll();
        } else {
            this.current = null;
        }
    }
}
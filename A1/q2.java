import java.util.Random;
import java.util.Stack;

class Node {
    Node leftChild;
    Node rightChild;
    Node next;
    Node previous;

    String name;

    public Node(String name) {
        this.name = name;
    }
}

class Thread0 extends Thread {

    public void run() {
        while (!q2.stop) {
            System.out.print("*");
            Node current = q2.head;
            while (current != null) {

                System.out.print(" " + current.name);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                current = current.next;
            }
            System.out.println();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Thread1 extends Thread {
    private Random random;

    public Thread1() {
        this.random = new Random();
    }

    public void run() {
        while (!q2.stop) {

            Node current = q2.head;
            while (current != null) {

                if (random.nextInt(10) == 0) {
                    String name = q2.generateName();
                    Node left = new Node(name);
                    Node right = new Node(name.toUpperCase());
                    current.leftChild = left;
                    current.rightChild = right;

                    right.next = current.next;
                    if (right.next == null)
                        q2.tail = right;
                    else
                        right.next.previous = right;

                    left.next = right;
                    right.previous = left;
                    left.previous = current.previous;
                    if (left.previous == null)
                        q2.head = left;
                    else
                        left.previous.next = left;
                }
                current = current.next;
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

class Thread2 extends Thread {

    public void run() {

        while (!q2.stop) {

            q2.count = 0;

            Stack<Node> stack = new Stack<>();

            stack.push(q2.root);

            while (!stack.isEmpty()) {
                Node current = stack.pop();
                q2.count++;
                if (current.rightChild != null)
                    stack.push(current.rightChild);
                if (current.leftChild != null)
                    stack.push(current.leftChild);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println();
            System.out.println(q2.count);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class q2 {

    static Node root;
    volatile static Node head;
    volatile static Node tail;

    static int count;

    static boolean stop;

    public static void main(String[] args) throws InterruptedException {

        stop = false;

        root = new Node("root");
        String name = generateName();
        Node left = new Node(name);
        Node right = new Node(name.toUpperCase());
        root.leftChild = left;
        root.rightChild = right;
        left.next = right;
        right.previous = left;

        head = left;
        tail = right;

        Thread0 thread0 = new Thread0();
        Thread1 thread1 = new Thread1();
        Thread2 thread2 = new Thread2();
        thread0.start();
        thread1.start();
        thread2.start();

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 5000) {
            Thread.sleep(1);
        }
        stop = true;
        thread0.join();
        thread1.join();
        thread2.join();

        System.out.println();
        System.out.println(count);
        System.out.println();

        Node current = head;
        while (current != null) {
            System.out.print(current.name + " ");
            current = current.next;
        }
        System.out.println();
    }

    public static String generateName() {
        char[] chars = new char[5];
        Random random = new Random();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (random.nextInt(26) + 97);
        }
        return String.valueOf(chars);
    }
}

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Point {
    private double x;
    private double y;
    private List<Point> adjacentPointList;
    private boolean lock;
    private long id;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
        this.adjacentPointList = new ArrayList<>();
        this.lock = false;
        this.id = -1;
    }

    static boolean checkDuplicate(double x, double y, List<Point> list) {
        return list.stream().anyMatch(point -> point.x == x && point.y == y);
    }

    synchronized boolean getLock(long id) {
        if (this.lock)
            return false;
        this.lock = true;
        this.id = id;
//        System.out.println(id + " got the lock");
        return true;
    }

    synchronized void releaseLock(long id) {
        if (this.id == id) {
            this.lock = false;
            this.id = -1;
        }
    }

    List<Point> getAdjacentPointList(long id) {
        if (!this.lock || this.id != id)
            return null;

        return this.adjacentPointList;
    }
}

class TriangulateThread extends Thread {
    private List<Point> pointList;
    private int k;
    private Random rand;

    TriangulateThread(List<Point> pointList, int k) {
        this.pointList = pointList;
        this.k = k;
        this.rand = new Random();
    }

    @Override
    public void run() {


//        System.out.println(this.getId() + "'k: " + this.k);
        while (this.k > 0) {
            int index1 = rand.nextInt(pointList.size());
            int index2 = rand.nextInt(pointList.size());
            while (index2 == index1) {
                index2 = rand.nextInt(pointList.size());
            }
            addAnEdge(pointList.get(index1), pointList.get(index2));
//            System.out.println(this.getId() + "'k: " + this.k);
        }

    }

    private void addAnEdge(Point a, Point b) {
        long id = this.getId();
        while (!(a.getLock(id) && b.getLock(id))) {
            a.releaseLock(id);
            b.releaseLock(id);
        }

//        System.out.println(id + " locked");
        List<Point> list1 = a.getAdjacentPointList(id);
        List<Point> list2 = b.getAdjacentPointList(id);

        if (list1.contains(b)) {
            this.k--;
            if (this.k == 0) {
                System.out.println(this.getId() + ": " + q1a.numEdges);
                System.exit(0);
            }
            a.releaseLock(id);
            b.releaseLock(id);
//            System.out.println(id + " released the lock");
            return;
        }

        list1.add(b);
        list2.add(a);
        a.releaseLock(id);
        b.releaseLock(id);

//        System.out.println(id + " released the lock");
        q1a.incrementNumEdges();
    }
}

class q1a {

    volatile static int numEdges = 0;

    public static void main(String[] args) {

        if (args.length <= 0) {
            System.out.println("Wrong number of args");
            return;
        }

        int n = Integer.parseInt(args[0]);
        int t = Integer.parseInt(args[1]);
        int k = Integer.parseInt(args[2]);

        List<Point> pointList = new ArrayList<>();

        Random rand = new Random();

        // init non overlapping points
        for (int i = 0; i < n; i++) {
            double x;
            double y;
            do {
                x = rand.nextDouble() * 1000;
                y = rand.nextDouble() * 1000;
            }
            while (Point.checkDuplicate(x, y, pointList));
            pointList.add(new Point(x, y));
        }

        for (int i = 0; i < t; i++) {
            Thread thread = new TriangulateThread(pointList, k);
            thread.start();
        }

    }

    synchronized static void incrementNumEdges() {
        q1a.numEdges++;
    }
}
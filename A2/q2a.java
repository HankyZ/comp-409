import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class SpiderThread extends Thread {

    private Random rand;
    private List<Point> positionList;
    private int numMovesMade;

    SpiderThread() {
        refreshRestingPointList();

        this.rand = new Random();
        this.positionList = selectPosition();
        this.numMovesMade = 0;

        if (this.positionList != null) {
            for (Point p : this.positionList)
                p.occupied = true;
        }
    }

    @Override
    public void run() {

        while (!q2a.timeIsUp()) {

            refreshRestingPointList();

            List<Point> destinationPositionList = selectPosition();

            if (destinationPositionList != null) {

                boolean locked = true;

                for (Point p : destinationPositionList) {
                    if (!p.getLock(this.getId())) {
                        for (Point pt : destinationPositionList) {
                            pt.releaseLock(this.getId());
                            locked = false;
                        }
                        break;
                    }
                }

                if (locked) {
                    moveToPosition(destinationPositionList);

                    for (Point pt : destinationPositionList) {
                        pt.releaseLock(this.getId());
                    }
                }
            }

            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("spider " + this.getId() + " made " + this.numMovesMade + " moves");
    }

    synchronized private void refreshRestingPointList() {

        q2a.restingPointList = new ArrayList<>(q2a.web);

        // remove invalid resting points from the list
        int i = 0;
        while (i < q2a.restingPointList.size()) {
            Point point = q2a.restingPointList.get(i);
            if (point.occupied) {
                q2a.restingPointList.remove(i);
                continue;
            }
            if (point.adjacentPointList.size() < 3) {
                q2a.restingPointList.remove(i);
                continue;
            }
            int numAvailablePoints = 1;
            for (Point p : point.adjacentPointList) {
                if (!p.occupied) {
                    numAvailablePoints++;
                }
                if (numAvailablePoints >= 4)
                    break;
            }
            i++;
        }
    }

    private List<Point> selectPosition() {
        if (q2a.restingPointList.size() == 0) {
            return null;
        }

        List<Point> positionList = new ArrayList<>();

        Point bodyPoint = q2a.restingPointList.get(rand.nextInt(q2a.restingPointList.size()));
        positionList.add(bodyPoint);

        while (positionList.size() < 4) {
            Point legPoint = bodyPoint.adjacentPointList.get(rand.nextInt(bodyPoint.adjacentPointList.size()));
            if (legPoint.occupied)
                continue;
            positionList.add(legPoint);
        }

        if (positionList.size() != 4)
            return null;

        return positionList;
    }

    private void moveToPosition(List<Point> destinationPositionList) {
        for (Point p : destinationPositionList) {
            p.occupied = true;
        }

        for (Point p : this.positionList) {
            p.occupied = false;
        }

        this.positionList = destinationPositionList;
        this.numMovesMade++;
    }
}

public class q2a {

    static List<Point> web;
    static List<Point> restingPointList;

    private static long startTime;

    private static long m;

    public static void main(String[] args) throws InterruptedException {

        if (args.length <= 0) {
            System.out.println("Wrong number of args");
            return;
        }

        int n = Integer.parseInt(args[0]);
        int t = Integer.parseInt(args[1]);
        int k = Integer.parseInt(args[2]);
        int m = Integer.parseInt(args[3]);

        web = q1a.formWeb(n, t, k);
        restingPointList = new ArrayList<>(web);

        q2a.m = m * 1000;

        List<Thread> threadList = new ArrayList<>();

        startTime = System.currentTimeMillis();
        for (int i = 0; i < t; i++) {
            Thread thread = new SpiderThread();
            threadList.add(thread);
            thread.start();
        }

        for (Thread thread : threadList) {
            thread.join();
        }
    }

    synchronized static boolean timeIsUp() {
        long currentTime = System.currentTimeMillis();
        return currentTime - startTime > m;
    }
}

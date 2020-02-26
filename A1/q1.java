import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class ThreadManager {

    private int numThreads;
    private int numRectangles;

    private int k;

    private BufferedImage bufferedImage;
    private boolean[][] image;

    private boolean lock;

    private List<Thread> threadList;

    public ThreadManager(BufferedImage bufferedImage, int n, int k) {
        this.numThreads = n;
        this.numRectangles = k;
        this.k = 0;
        this.bufferedImage = bufferedImage;
        this.image = new boolean[bufferedImage.getHeight()][bufferedImage.getWidth()];
        this.threadList = new ArrayList<>();
    }

    public void startThreads() {
        for (int i = 0; i < numThreads; i++) {
            RectangleThread thread = new RectangleThread(this, this.bufferedImage, this.numRectangles);
            this.threadList.add(thread);
            thread.start();
        }
    }

    public void waitForThreads() throws InterruptedException {
        for (Thread thread : this.threadList) {
            thread.join();
        }
    }

    public synchronized int incrementK() {
        this.k++;
        return this.k;
    }

    public synchronized boolean getLock() {
        if (this.lock)
            return false;
        this.lock = true;
        return true;
    }

    public void freeLock() {
        this.lock = false;
    }

    public boolean isRectangleFree(int x, int y, int w, int h) {

        if (x > w) {
            int tmp = w;
            w = x;
            x = tmp;
        }

        if (y > h) {
            int tmp = h;
            h = y;
            y = tmp;
        }

        for (int i = y; i < h; i++) {
            for (int j = x; j < w; j++) {
                if (image[i][j])
                    return false;
            }
        }

        return true;
    }

    public void reserveRectangle(int x, int y, int w, int h) {

        for (int i = y; i < h + 1; i++) {
            for (int j = x; j < w + 1; j++) {
                image[i][j] = true;
            }
        }
    }

    public void freeRectangle(int x, int y, int w, int h) {

        for (int i = y; i < h + 1; i++) {
            for (int j = x; j < w + 1; j++) {
                image[i][j] = false;
            }
        }
    }
}

class RectangleThread extends Thread {

    private ThreadManager threadManager;
    private BufferedImage image;

    private int w;
    private int h;
    private int k;

    public RectangleThread(ThreadManager threadManager, BufferedImage image, int k) {
        this.threadManager = threadManager;
        this.image = image;
        this.w = image.getWidth();
        this.h = image.getHeight();
        this.k = k;
    }

    @Override
    public void run() {

        while (threadManager.incrementK() <= this.k) {
            drawRectangle();
        }
    }

    private void drawRectangle() {
        Random random = new Random();
        int start = random.nextInt(4);
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        int rectangleW = random.nextInt(w);
        int rectangleH = random.nextInt(h);

        int rectangleX;
        int rectangleY;

        // set starting point to be one of the corners
        switch (start) {
            case 0:
                rectangleX = 0;
                rectangleY = 0;
                if (rectangleW == 0)
                    rectangleW += 2;
                if (rectangleH == 0)
                    rectangleH += 2;
                break;
            case 1:
                rectangleX = w - 1;
                rectangleY = 0;
                if (rectangleW == w - 1)
                    rectangleW -= 2;
                if (rectangleH == 0)
                    rectangleH += 2;
                break;
            case 2:
                rectangleX = w - 1;
                rectangleY = h - 1;
                if (rectangleW == w - 1)
                    rectangleW -= 2;
                if (rectangleH == h - 1)
                    rectangleH -= 2;
                break;
            default:
                rectangleX = 0;
                rectangleY = h - 1;
                if (rectangleW == 0)
                    rectangleW += 2;
                if (rectangleH == h - 1)
                    rectangleH -= 2;
                break;
        }

        // make sure x, y are smaller w, h
        if (rectangleX > rectangleW) {
            int tmp = rectangleX;
            rectangleX = rectangleW;
            rectangleW = tmp;
        }
        if (rectangleY > rectangleH) {
            int tmp = rectangleY;
            rectangleY = rectangleH;
            rectangleH = tmp;
        }

        boolean successfullyReserved = false;

        do {
            while (!threadManager.getLock()) ;

            if (threadManager.isRectangleFree(rectangleX, rectangleY, rectangleW, rectangleH)) {
                threadManager.reserveRectangle(rectangleX, rectangleY, rectangleW, rectangleH);
                successfullyReserved = true;
                threadManager.freeLock();
            } else
                threadManager.freeLock();
        } while (!successfullyReserved);

        this.drawBorders(rectangleX, rectangleY, rectangleW, rectangleH);
        this.fillRectangle(rectangleX, rectangleY, rectangleW, rectangleH, new Color(r, g, b).getRGB());

        threadManager.freeRectangle(rectangleX, rectangleY, rectangleW, rectangleH);
    }

    private void drawBorders(int rectangleX, int rectangleY, int rectangleW, int rectangleH) {

        int black = new Color(0, 0, 0).getRGB();
        for (int i = rectangleX; i < rectangleW; i++)
            this.image.setRGB(i, rectangleY, black);
        for (int i = rectangleY; i < rectangleH; i++)
            this.image.setRGB(rectangleX, i, black);
        for (int i = rectangleY; i < rectangleH; i++)
            this.image.setRGB(rectangleW, i, black);
        for (int i = rectangleX; i < rectangleW + 1; i++)
            this.image.setRGB(i, rectangleH, black);
    }

    private void fillRectangle(int rectangleX, int rectangleY, int rectangleW, int rectangleH, int rgb) {

        for (int i = rectangleX + 1; i < rectangleW; i++) {
            for (int j = rectangleY + 1; j < rectangleH; j++)
                this.image.setRGB(i, j, rgb);
        }
    }
}

public class q1 {

    public static void main(String[] args) {
        try {

            if (args.length <= 0) {
                System.out.println("Wrong number of args");
                return;
            }

            long start = System.currentTimeMillis();

            int w = Integer.parseInt(args[0]);
            int h = Integer.parseInt(args[1]);
            int n = Integer.parseInt(args[2]);
            int k = Integer.parseInt(args[3]);

            BufferedImage outputImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            // ------------------------------------
            // Your code would go here


            ThreadManager threadManager = new ThreadManager(outputImage, n, k);
            threadManager.startThreads();

            try {
                threadManager.waitForThreads();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            File outputFile = new File("outputImage.png");
            ImageIO.write(outputImage, "png", outputFile);

            long end = System.currentTimeMillis();

            long executionTime = end - start;

            System.out.println("Execution time = " + executionTime);

        } catch (Exception e) {
            System.out.println("ERROR " + e);
            e.printStackTrace();
        }
    }
}

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newFixedThreadPool;

class q1 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        if (args.length <= 0) {
            System.out.println("Wrong number of args");
            return;
        }

        int n = Integer.parseInt(args[0]);
        int t = Integer.parseInt(args[1]);
        long s = Long.parseLong(args[2]);

        Bracket.construct(n, s);

        long startTime = System.currentTimeMillis();

        ExecutorService executorService = newFixedThreadPool(t);
        Future<char[]> future = executorService.submit(() -> Bracket.construct(n, s));
        char[] result = future.get();

        long endTime = System.currentTimeMillis();

        
    }

}
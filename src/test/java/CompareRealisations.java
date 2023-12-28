import me.tongfei.progressbar.ProgressBar;
import org.example.pool.ThreadPool;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CompareRealisations {
    /*
    Операция создания потока операционной системы достаточно дорогое, и по этому плохо подходит для короткоживущих задач
    * thread pool подойдет, если у нас легковестные и короткоживущие треды, которые не проводят много сложных вычислений.
    * если мы порождаем пачку потоков, значение которых сильно превосходит количество ядер процессора из-за чего
        планировщик будет вынужден постоянно переключать контекст (особенно, если потоки не связаны с операциями ввода-вывода)
     */

    class SimpleNumberFounder implements Runnable {
        private List<Boolean> matrix = new ArrayList<>();
        private int n;

        public SimpleNumberFounder(int n) {
            this.n = n;
        }


        @Override
        public void run() {
            for (int i = 0; i < n; i++) {
                matrix.add(true);
            }
            matrix.set(2, true);
            for (int i = 2; i < n; i++) {
                if (matrix.get(i)) {
                    for (int j = i + i; j < n; j += i) {
                        matrix.set(j, false);
                    }
                }
            }
            matrix.stream()
                    .filter(i -> i)
                    .count();
        }
    }


    @Test
    void compare() throws InterruptedException {
        int N_THREADS = 1000;
        int LIST_SIZE = 90000;
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < N_THREADS; i++) {
            threads.add(new Thread(new SimpleNumberFounder(LIST_SIZE)));
        }

        Instant start = Instant.now();
        for (var x : threads) {
            x.start();
        }

        for (var i : threads) {
            i.join();
        }
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        // что бы не получать ошибку, связанную с переполнением кучи
        threads.clear();

        List<Runnable> interfaces = new ArrayList<>();
        for (int i = 0; i < N_THREADS; i++) {
            interfaces.add(new SimpleNumberFounder(LIST_SIZE));
        }
        var pool = new ThreadPool(Runtime.getRuntime().availableProcessors());
        Instant startPool = Instant.now();
        interfaces.forEach(pool::execute);
        pool.joinPool();
        Instant finishPool = Instant.now();
        System.out.println("elapsed time in threads (ms): " + timeElapsed);
        System.out.println("elapsed time in thread pool(ms): " + Duration.between(startPool, finishPool).toMillis());

    }

}

package task1;

public class Task1 {
    private static char ch = 'A';
    private final static Object monitor = new Object();


    public static class MultiThreadClass implements Runnable {
        private char chCurrent;
        private char chNext;

        public MultiThreadClass(char chCurrent, char chNext) {
            this.chCurrent = chCurrent;
            this.chNext = chNext;
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                synchronized (monitor) {
                    try {
                        while (ch != chCurrent)
                            monitor.wait();
                        System.out.print(chCurrent);
                        ch = chNext;
                        Thread.sleep(1);
                        monitor.notifyAll();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new MultiThreadClass('A', 'B')).start();
        new Thread(new MultiThreadClass('B', 'C')).start();
        new Thread(new MultiThreadClass('C', 'A')).start();
    }

}


package lesson5;


import java.util.concurrent.Semaphore;

public abstract class Stage {
    protected int length;
    protected String description;
    final Semaphore tunnelAccess = new Semaphore(2);

    public String getDescription() {
        return description;
    }

    public Semaphore getTunnelAccess() {
        return tunnelAccess;
    }

    public abstract void go(Car c);

}

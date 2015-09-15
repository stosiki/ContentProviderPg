package study.stosiki.com.contentproviderpg.events;

/**
 * Created by 1 on 15/09/2015.
 */
public class SimpleEvent {
    private long timestamp;

    public SimpleEvent(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

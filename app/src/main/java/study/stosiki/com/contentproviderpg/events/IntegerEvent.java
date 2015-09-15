package study.stosiki.com.contentproviderpg.events;

/**
 * Created by 1 on 15/09/2015.
 */
public class IntegerEvent extends SimpleEvent {
    private int value;
    public IntegerEvent(long timestamp, int value) {
        super(timestamp);
        this.value = value;
    }
}

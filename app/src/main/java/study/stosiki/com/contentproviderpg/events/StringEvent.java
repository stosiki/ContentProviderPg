package study.stosiki.com.contentproviderpg.events;

/**
 * Created by 1 on 15/09/2015.
 */
public class StringEvent extends SimpleEvent {
    private String comment;

    public StringEvent(long timestamp, String comment) {
        super(timestamp);
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
}

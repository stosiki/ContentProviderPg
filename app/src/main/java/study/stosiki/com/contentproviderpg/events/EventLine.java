package study.stosiki.com.contentproviderpg.events;

import java.util.ArrayList;

/**
 * Created by 1 on 15/09/2015.
 */
public class EventLine {
    public static final int LINE_TYPE_SIMPLE = 0;
    public static final int LINE_TYPE_INTEGER = 1;
    public static final int LINE_TYPE_STRING = 2;


    private String title;
    private int type;
    private ArrayList events;
    private boolean aggregatedDaily;

    public EventLine(String title, int type, boolean aggregatedDaily) {
        this.title = title;
        this.type = type;
        this.aggregatedDaily = aggregatedDaily;
        this.events = new ArrayList();
    }

    public void addEvent(SimpleEvent event) {
        events.add(event);
    }

    public String getTitle() {
        return title;
    }

    public int getType() {
        return type;
    }

    public ArrayList<SimpleEvent> getEvents() {
        return events;
    }

    public boolean isAggregatedDaily() {
        return aggregatedDaily;
    }
}

package study.stosiki.com.contentproviderpg.events;

import java.util.ArrayList;

/**
 * Created by 1 on 15/09/2015.
 */
public class EventLine {
    public static final int LINE_TYPE_SIMPLE = 0;
    public static final int LINE_TYPE_INTEGER = 1;
    public static final int LINE_TYPE_STRING = 2;

    public static final int AGGREGATE_NONE = 0;
    public static final int AGGREGATE_DAILY = 1;


    private String title;
    private int type;
    private ArrayList events;
    private int aggregate;
    private String color;

    public EventLine(String title, int type, String color, int aggregate) {
        this.title = title;
        this.type = type;
        this.aggregate = aggregate;
        this.color = color;
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

    public int getAggregate() {
        return aggregate;
    }

    public String getColor() {
        return color;
    }
}

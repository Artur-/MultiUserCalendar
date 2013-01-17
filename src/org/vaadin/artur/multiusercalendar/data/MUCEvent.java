package org.vaadin.artur.multiusercalendar.data;

import java.text.DateFormat;

import com.vaadin.addon.calendar.event.BasicEvent;

public class MUCEvent extends BasicEvent implements Cloneable {

    private String privateEventOwner;

    public String getPrivateEventOwner() {
        return privateEventOwner;
    }

    public void setPrivateEventOwner(String privateEvent) {
        this.privateEventOwner = privateEvent;
    }

    @Override
    public MUCEvent clone() throws CloneNotSupportedException {
        return (MUCEvent) super.clone();
    }

    @Override
    public String toString() {
        return getTime() + ": " + getCaption();
    }

    public String getTime() {
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT);
        String time = formatter.format(getStart()) + "-"
                + formatter.format(getEnd());

        return time;

    }

}

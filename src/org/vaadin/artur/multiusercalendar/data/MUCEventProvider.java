package org.vaadin.artur.multiusercalendar.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.vaadin.artur.multiusercalendar.ui.MultiusercalendarUI;

import com.vaadin.addon.calendar.event.BasicEvent;
import com.vaadin.addon.calendar.event.CalendarEvent;
import com.vaadin.addon.calendar.event.CalendarEventProvider;

public class MUCEventProvider implements CalendarEventProvider {

    private static List<BasicEvent> events = new ArrayList<BasicEvent>();

    public interface EventUpdateListener {
        public void userJoined(MultiusercalendarUI sourceUI);

        public void eventAdded(MultiusercalendarUI ui, MUCEvent event);

        public void eventRemoved(MultiusercalendarUI ui, MUCEvent event);

        public void eventMoved(MultiusercalendarUI ui, MUCEvent event);

        public void eventUpdated(MultiusercalendarUI ui, MUCEvent event);

        public void eventResized(MultiusercalendarUI ui, MUCEvent event);
    }

    private static List<EventUpdateListener> listeners = new ArrayList<EventUpdateListener>();
    private static List<EventSetChangeListener> eventSetChangeListeners = new ArrayList<EventSetChangeListener>();

    private MultiusercalendarUI ui;

    public MUCEventProvider(MultiusercalendarUI ui) {
        this.ui = ui;
        addListener(ui);
    }

    private void addListener(EventUpdateListener listener) {
        synchronized (MUCEventProvider.class) {
            listeners.add(listener);
        }
    }

    public void addEvent(MUCEvent event) {
        synchronized (events) {
            events.add(event);
        }

        if (event.getPrivateEventOwner() == null)
            fireEventAdded(ui, event);

    }

    public void removeEvent(MUCEvent event) {
        synchronized (events) {
            events.remove(event);
        }

        if (event.getPrivateEventOwner() == null)
            fireEventRemoved(ui, event);
    }

    @Override
    public List<CalendarEvent> getEvents(Date from, Date to) {

        List<CalendarEvent> matchingEvents = new ArrayList<CalendarEvent>();
        Object[] available = events.toArray();
        for (Object o : available) {
            MUCEvent e = (MUCEvent) o;
            if (e.getStart().after(to) || e.getEnd().before(from))
                continue;

            // Show private events only to the owner
            if (e.getPrivateEventOwner() != null
                    && !e.getPrivateEventOwner().equals(ui.toString())) {
                continue;
            }

            matchingEvents.add(e);
        }

        return matchingEvents;
    }

    public void fireEventUpdated(MultiusercalendarUI ui, BasicEvent event) {
        synchronized (MUCEventProvider.class) {
            for (EventUpdateListener l : listeners) {
                l.eventUpdated(ui, (MUCEvent) event);
            }
        }
    }

    public void fireEventAdded(MultiusercalendarUI ui, BasicEvent event) {
        synchronized (MUCEventProvider.class) {
            for (EventUpdateListener l : listeners) {
                l.eventAdded(ui, (MUCEvent) event);
            }
        }
    }

    public void fireEventRemoved(MultiusercalendarUI ui, BasicEvent event) {
        synchronized (MUCEventProvider.class) {
            for (EventUpdateListener l : listeners) {
                l.eventRemoved(ui, (MUCEvent) event);
            }
        }
    }

    public void fireEventMoved(MultiusercalendarUI ui, BasicEvent event) {
        synchronized (MUCEventProvider.class) {
            for (EventUpdateListener l : listeners) {
                l.eventMoved(ui, (MUCEvent) event);
            }
        }
    }

    public void fireEventResized(MultiusercalendarUI ui, BasicEvent event) {
        synchronized (MUCEventProvider.class) {
            for (EventUpdateListener l : listeners) {
                l.eventResized(ui, (MUCEvent) event);
            }
        }
    }

    public void fireUserJoined(MultiusercalendarUI ui) {
        synchronized (MUCEventProvider.class) {
            for (EventUpdateListener l : listeners) {
                l.userJoined(ui);
            }
        }
    }

    public void addedEvent(MUCEvent event) {
        if (event.getPrivateEventOwner() == null) {
            fireEventAdded(ui, event);
        }
    }

    public void updatedEvent(MUCEvent event) {
        if (event.getPrivateEventOwner() == null) {
            fireEventUpdated(ui, event);
        }
    }

    public void moveEvent(BasicEvent event, Date newStart) {
        long diff = newStart.getTime() - event.getStart().getTime();
        event.setStart(newStart);
        event.setEnd(new Date(event.getEnd().getTime() + diff));

        fireEventMoved(ui, event);
    }

    public void resizeEvent(BasicEvent event, Date newStartTime, Date newEndTime) {
        event.setStart(newStartTime);
        event.setEnd(newEndTime);

        fireEventResized(ui, event);

    }

    public void removeUI(MultiusercalendarUI ui) {
        synchronized (MUCEventProvider.class) {
            listeners.remove(ui);
        }
    }

}

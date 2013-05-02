package org.vaadin.artur.multiusercalendar.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.artur.multiusercalendar.ui.MultiusercalendarUI;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.components.calendar.event.BasicEvent;
import com.vaadin.ui.components.calendar.event.CalendarEvent;
import com.vaadin.ui.components.calendar.event.CalendarEventProvider;

@SuppressWarnings("serial")
public class MUCEventProvider implements CalendarEventProvider {

    private static List<BasicEvent> events = new ArrayList<BasicEvent>();

    public interface MUCEventListener {
        public void userJoined(MultiusercalendarUI sourceUI);

        public void eventAdded(MultiusercalendarUI ui, MUCEvent event);

        public void eventRemoved(MultiusercalendarUI ui, MUCEvent event);

        public void eventMoved(MultiusercalendarUI ui, MUCEvent event);

        public void eventUpdated(MultiusercalendarUI ui, MUCEvent event);

        public void eventResized(MultiusercalendarUI ui, MUCEvent event);
    }

    private static List<MUCEventListener> listeners = new ArrayList<MUCEventListener>();

    private MultiusercalendarUI ui;

    public MUCEventProvider(MultiusercalendarUI ui) {
        setUI(ui);
    }

    public void addEvent(MUCEvent event) {
        synchronized (MUCEventProvider.class) {
            events.add(event);
        }

        if (event.getPrivateEventOwner() == null)
            fireEventAdded(ui, event);

    }

    public void removeEvent(MUCEvent event) {
        synchronized (MUCEventProvider.class) {
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

    public void fireEventAdded(final MultiusercalendarUI ui,
            final MUCEvent event) {
        fireEvent(new MUCEventDispatcher() {
            @Override
            public void dispatch(MUCEventListener listener) {
                listener.eventAdded(ui, event);
            }
        });
    }

    public void fireEventRemoved(final MultiusercalendarUI ui,
            final MUCEvent event) {
        fireEvent(new MUCEventDispatcher() {
            @Override
            public void dispatch(MUCEventListener listener) {
                listener.eventRemoved(ui, event);
            }
        });
    }

    public void fireEventMoved(final MultiusercalendarUI ui,
            final MUCEvent event) {
        fireEvent(new MUCEventDispatcher() {
            @Override
            public void dispatch(MUCEventListener listener) {
                listener.eventMoved(ui, event);
            }
        });
    }

    public void fireEventResized(final MultiusercalendarUI ui,
            final MUCEvent event) {
        fireEvent(new MUCEventDispatcher() {
            @Override
            public void dispatch(MUCEventListener listener) {
                listener.eventResized(ui, event);
            }
        });
    }

    public void fireUserJoined(final MultiusercalendarUI ui) {
        fireEvent(new MUCEventDispatcher() {
            @Override
            public void dispatch(MUCEventListener listener) {
                listener.userJoined(ui);
            }
        });
    }

    public void addedEvent(MUCEvent event) {
        if (event.getPrivateEventOwner() == null) {
            fireEventAdded(ui, event);
        }
    }

    public void updatedEvent(final MUCEvent event) {
        if (event.getPrivateEventOwner() == null) {
            fireEvent(new MUCEventDispatcher() {
                @Override
                public void dispatch(MUCEventListener listener) {
                    listener.eventUpdated(ui, event);
                }
            });
        }
    }

    /**
     * Sends the event in a separate thread to avoid dead locks.
     * 
     * @param eventDispatcher
     */
    private void fireEvent(final MUCEventDispatcher eventDispatcher) {
        final MUCEventListener[] listenerCopy;
        synchronized (MUCEventProvider.class) {
            listenerCopy = listeners.toArray(new MUCEventListener[] {});
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Workaround for Vaadin bug
                VaadinSession.setCurrent(null);
                
                for (MUCEventListener l : listenerCopy) {
                    try {
                        eventDispatcher.dispatch(l);
                    } catch (Exception e) {
                        getLogger().log(Level.WARNING, "Failed to send event",
                                e);
                    }
                }
            }

        }).start();

    }

    public void moveEvent(MUCEvent event, Date newStart) {
        long diff = newStart.getTime() - event.getStart().getTime();
        event.setStart(newStart);
        event.setEnd(new Date(event.getEnd().getTime() + diff));

        fireEventMoved(ui, (MUCEvent) event);
    }

    public void resizeEvent(MUCEvent event, Date newStartTime, Date newEndTime) {
        event.setStart(newStartTime);
        event.setEnd(newEndTime);

        fireEventResized(ui, event);

    }

    public void setUI(MultiusercalendarUI ui) {
        this.ui = ui;
        synchronized (MUCEventProvider.class) {
            listeners.add(ui.getMUCEventListener());
        }

    }

    public void removeUI(MultiusercalendarUI ui) {
        synchronized (MUCEventProvider.class) {
            listeners.remove(ui.getMUCEventListener());
        }
    }

    private static Logger getLogger() {
        return Logger.getLogger(MUCEventProvider.class.getName());
    }

}

package org.vaadin.artur.multiusercalendar.ui;

import java.util.Date;

import org.vaadin.artur.multiusercalendar.data.MUCEvent;
import org.vaadin.artur.multiusercalendar.data.MUCEventProvider;
import org.vaadin.artur.multiusercalendar.data.MUCEventProvider.MUCEventListener;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Calendar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventClick;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventClickHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventMoveHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventResize;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventResizeHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.MoveEvent;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.RangeSelectEvent;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.RangeSelectHandler;

@Theme("multiusercalendartheme")
@Push
public class MultiusercalendarUI extends UI {

    private static final int DAY_IN_MS = 1000 * 3600 * 24;
    private Calendar calendar;
    private static String[] eventStyles = new String[] { "green", "blue",
            "red", "yellow", "white", "cyan", "magenta" };
    private static int styleNr = 0;
    private MUCEventProvider eventProvider;

    private String eventStyle;
    protected String userName;

    private MUCEventListener mucEventListener = new UIEventUpdateListener(this);

    private EventResizeHandler eventResizeHandler = new EventResizeHandler() {

        @Override
        public void eventResize(EventResize e) {
            MUCEvent event = (MUCEvent) e.getCalendarEvent();
            eventProvider.resizeEvent(event, e.getNewStart(), e.getNewEnd());

        }

    };
    private RangeSelectHandler rangeSelectHandler = new RangeSelectHandler() {
        @Override
        public void rangeSelect(RangeSelectEvent e) {
            final MUCEvent event = new MUCEvent();
            event.setPrivateEventOwner(toString());
            event.setCaption("");
            event.setStart(e.getStart());
            event.setEnd(e.getEnd());
            event.setStyleName(eventStyle);
            eventProvider.addEvent(event);
            calendar.markAsDirty();

            final EditPopup editPopup = new EditPopup(event);
            editPopup.addCloseListener(new CloseListener() {
                @Override
                public void windowClose(CloseEvent e) {
                    if (editPopup.isOk()) {
                        event.setCaption(editPopup.getEventCaption());
                        event.setPrivateEventOwner(null);
                        eventProvider.addedEvent(event);
                    } else {
                        eventProvider.removeEvent(event);
                        calendar.markAsDirty();
                    }
                }
            });
            addWindow(editPopup);
        }

    };
    private EventClickHandler eventClickHandler = new EventClickHandler() {

        @Override
        public void eventClick(EventClick arg0) {
            final MUCEvent event = (MUCEvent) arg0.getCalendarEvent();
            final EditPopup editPopup = new EditPopup(event);
            editPopup.addCloseListener(new CloseListener() {

                @Override
                public void windowClose(CloseEvent e) {
                    if (editPopup.isOk()) {
                        event.setCaption(editPopup.getEventCaption());
                        event.setPrivateEventOwner(null);
                        eventProvider.updatedEvent(event);
                    }
                }
            });
            addWindow(editPopup);
        }

    };
    private EventMoveHandler eventMoveHandler = new EventMoveHandler() {

        @Override
        public void eventMove(MoveEvent e) {
            MUCEvent event = (MUCEvent) e.getCalendarEvent();
            eventProvider.moveEvent(event, e.getNewStart());
        }
    };

    @Override
    public void init(VaadinRequest request) {
        eventStyle = eventStyles[styleNr++ % eventStyles.length];

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        setContent(layout);

        Calendar cal = constructUI();
        layout.addComponent(cal);
        layout.setExpandRatio(cal, 1);

        final UserNameWindow userNameWindow = new UserNameWindow();
        userNameWindow.addCloseListener(new CloseListener() {

            @Override
            public void windowClose(CloseEvent e) {
                if (userNameWindow.getUserName() == null) {
                    Notification.show("Come on...");
                    addWindow(userNameWindow);
                } else {
                    userName = userNameWindow.getUserName();
                    eventProvider.fireUserJoined(MultiusercalendarUI.this);
                }
            }
        });

        addWindow(userNameWindow);
    }

    private Calendar constructUI() {
        eventProvider = new MUCEventProvider(this);
        calendar = new Calendar(eventProvider);
        calendar.setSizeFull();
        calendar.setImmediate(true);

        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(new Date());
        c.set(java.util.Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        Date start = c.getTime();
        calendar.setStartDate(start);
        Date end = new Date(start.getTime() + DAY_IN_MS * 6);
        calendar.setEndDate(end);
        calendar.setHandler(rangeSelectHandler);
        calendar.setHandler(eventMoveHandler);
        calendar.setHandler(eventClickHandler);
        calendar.setHandler(eventResizeHandler);

        return calendar;
    }

    @Override
    public void detach() {
        super.detach();
        eventProvider.removeUI(this);
    }

    public void redrawCalendar() {
        calendar.markAsDirty();
    }

    public MUCEventListener getMUCEventListener() {
        return mucEventListener;
    }

}

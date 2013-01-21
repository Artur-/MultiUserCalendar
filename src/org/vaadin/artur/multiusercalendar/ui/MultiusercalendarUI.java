package org.vaadin.artur.multiusercalendar.ui;

import java.util.Date;

import org.vaadin.artur.icepush.ICEPush;
import org.vaadin.artur.multiusercalendar.data.MUCEvent;
import org.vaadin.artur.multiusercalendar.data.MUCEventProvider;
import org.vaadin.artur.multiusercalendar.data.MUCEventProvider.EventUpdateListener;

import com.vaadin.addon.calendar.ui.Calendar;
import com.vaadin.addon.calendar.ui.CalendarComponentEvents.EventClick;
import com.vaadin.addon.calendar.ui.CalendarComponentEvents.EventClickHandler;
import com.vaadin.addon.calendar.ui.CalendarComponentEvents.EventMoveHandler;
import com.vaadin.addon.calendar.ui.CalendarComponentEvents.EventResize;
import com.vaadin.addon.calendar.ui.CalendarComponentEvents.EventResizeHandler;
import com.vaadin.addon.calendar.ui.CalendarComponentEvents.MoveEvent;
import com.vaadin.addon.calendar.ui.CalendarComponentEvents.RangeSelectEvent;
import com.vaadin.addon.calendar.ui.CalendarComponentEvents.RangeSelectHandler;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

@Theme("multiusercalendartheme")
public class MultiusercalendarUI extends UI implements EventUpdateListener,
        RangeSelectHandler, EventMoveHandler, EventClickHandler,
        EventResizeHandler {

    private static final int DAY_IN_MS = 1000 * 3600 * 24;
    private Calendar calendar;
    private ICEPush push;
    private static String[] eventStyles = new String[] { "green", "blue",
            "red", "yellow", "white", "cyan", "magenta" };
    private static int styleNr = 0;
    private MUCEventProvider eventProvider;

    private String eventStyle;
    protected String userName;

    @Override
    public void init(VaadinRequest request) {
        eventStyle = eventStyles[styleNr++ % eventStyles.length];

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        setContent(layout);

        Calendar cal = constructUI();
        layout.addComponent(cal);
        layout.setExpandRatio(cal, 1);
        push = new ICEPush();
        push.extend(this);

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
        calendar.setHandler((RangeSelectHandler) this);
        calendar.setHandler((EventMoveHandler) this);
        calendar.setHandler((EventClickHandler) this);
        calendar.setHandler((EventResizeHandler) this);

        return calendar;
    }

    @Override
    public void eventAdded(MultiusercalendarUI sourceUI, MUCEvent event) {
        calendar.markAsDirty();
        if (sourceUI != this) {
            showTrayNotification(sourceUI.userName + " added event \""
                    + event.getCaption() + "\" at " + event.getTime());
        }
        push.push();
    }

    private void showTrayNotification(String message) {
        Notification n = new Notification(message, Type.TRAY_NOTIFICATION);
        n.show(getPage());
    }

    @Override
    public void eventUpdated(MultiusercalendarUI ui, MUCEvent event) {
        calendar.markAsDirty();
        if (ui != this) {
            showTrayNotification(ui.userName + " updated event \""
                    + event.getCaption() + "\" at " + event.getTime());
        }
        push.push();
    }

    @Override
    public void eventRemoved(MultiusercalendarUI ui, MUCEvent event) {
        calendar.markAsDirty();
        if (ui != this) {
            showTrayNotification(ui.userName + " removed event \""
                    + event.getCaption() + "\" (" + event.getTime() + ")");
        }
        push.push();
    }

    @Override
    public void eventMoved(MultiusercalendarUI ui, MUCEvent event) {
        calendar.markAsDirty();
        if (ui != this) {
            showTrayNotification(ui.userName + " moved event \""
                    + event.getCaption() + "\" to " + event.getTime());
        }
        push.push();
    }

    @Override
    public void eventResized(MultiusercalendarUI ui, MUCEvent event) {
        calendar.markAsDirty();

        if (ui != this) {
            showTrayNotification(ui.userName + " changed event \""
                    + event.getCaption() + "\" to " + event.getTime());
        }
        push.push();
    }

    @Override
    public void userJoined(MultiusercalendarUI sourceUI) {
        calendar.markAsDirty();

        if (sourceUI != this) {
            showTrayNotification(sourceUI.userName + " joined");
        }
        push.push();
    }

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

    @Override
    public void eventMove(MoveEvent e) {
        MUCEvent event = (MUCEvent) e.getCalendarEvent();
        eventProvider.moveEvent(event, e.getNewStart());
    }

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

    @Override
    public void eventResize(EventResize e) {
        MUCEvent event = (MUCEvent) e.getCalendarEvent();
        eventProvider
                .resizeEvent(event, e.getNewStartTime(), e.getNewEndTime());

    }

    @Override
    public void detach() {
        super.detach();
        eventProvider.removeUI(this);
    }

}

package org.vaadin.artur.multiusercalendar.ui;

import java.io.Serializable;

import org.vaadin.artur.multiusercalendar.data.MUCEvent;
import org.vaadin.artur.multiusercalendar.data.MUCEventProvider.MUCEventListener;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class UIEventUpdateListener implements MUCEventListener, Serializable {

    private MultiusercalendarUI ui;

    public UIEventUpdateListener(MultiusercalendarUI ui) {
        this.ui = ui;
    }

    @Override
    public void eventUpdated(MultiusercalendarUI sourceUI, MUCEvent event) {
        eventUpdate(sourceUI,
                sourceUI.userName + " updated event \"" + event.getCaption()
                        + "\" at " + event.getTime());
    }

    @Override
    public void eventRemoved(MultiusercalendarUI sourceUI, MUCEvent event) {
        eventUpdate(sourceUI,
                sourceUI.userName + " removed event \"" + event.getCaption()
                        + "\" (" + event.getTime() + ")");
    }

    @Override
    public void eventMoved(MultiusercalendarUI ui, MUCEvent event) {
        eventUpdate(ui, ui.userName + " moved event \"" + event.getCaption()
                + "\" to " + event.getTime());
    }

    @Override
    public void eventResized(MultiusercalendarUI sourceUI, MUCEvent event) {
        eventUpdate(sourceUI,
                sourceUI.userName + " changed event \"" + event.getCaption()
                        + "\" to " + event.getTime());
    }

    @Override
    public void userJoined(final MultiusercalendarUI sourceUI) {
        ui.access(new Runnable() {
            @Override
            public void run() {
                if (sourceUI != ui) {
                    showTrayNotification(sourceUI.userName + " joined");
                }
            }
        });
    }

    @Override
    public void eventAdded(MultiusercalendarUI sourceUI, MUCEvent event) {
        eventUpdate(sourceUI,
                sourceUI.userName + " added event \"" + event.getCaption()
                        + "\" at " + event.getTime());
    }

    private void eventUpdate(final MultiusercalendarUI sourceUI,
            final String notification) {
        ui.access(new Runnable() {
            @Override
            public void run() {
                // Force redrawing of calendar events
                ui.redrawCalendar();

                // show notification for everybody except creator
                if (sourceUI != ui) {
                    showTrayNotification(notification);
                }
            }
        });

    }

    private void showTrayNotification(String message) {
        Notification n = new Notification(message, Type.TRAY_NOTIFICATION);
        n.show(ui.getPage());
    }

}

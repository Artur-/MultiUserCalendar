package org.vaadin.artur.multiusercalendar.ui;

import java.text.DateFormat;

import org.vaadin.artur.multiusercalendar.data.MUCEvent;

import com.vaadin.event.ShortcutListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class EditPopup extends Window {

    private boolean ok = false;
    private TextField caption;

    public EditPopup(MUCEvent event) {
        super("Add event..");
        setSizeUndefined();
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeUndefined();
        layout.setMargin(true);
        setContent(layout);

        setModal(true);

        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT);
        Label fromTo = new Label(formatter.format(event.getStart()) + "-"
                + formatter.format(event.getEnd()));
        fromTo.setWidth(null);

        fromTo.setCaption("Time");

        caption = new TextField("Description");
        caption.setValue(event.getCaption());
        layout.addComponent(fromTo);
        layout.addComponent(caption);

        caption.focus();
        caption.addShortcutListener(new ShortcutListener(null, KeyCode.ENTER,
                null) {

            @Override
            public void handleAction(Object sender, Object target) {
                ok = true;
                close();
            }
        });

        addAction(new ShortcutListener(null, KeyCode.ESCAPE, null) {

            @Override
            public void handleAction(Object sender, Object target) {
                ok = false;
                close();
            }
        });
    }

    public boolean isOk() {
        return ok;
    }

    public String getEventCaption() {
        return (String) caption.getValue();
    }
}

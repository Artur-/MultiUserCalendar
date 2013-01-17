package org.vaadin.artur.multiusercalendar.ui;

import com.vaadin.event.ShortcutListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class UserNameWindow extends Window {

    private TextField name;

    public UserNameWindow() {
        super("Hello, who are you?");
        setSizeUndefined();
        setModal(true);
        name = new TextField((String) null);
        name.setWidth("200px");
        name.addShortcutListener(new ShortcutListener(null, KeyCode.ENTER, null) {

            @Override
            public void handleAction(Object sender, Object target) {
                close();
            }
        });
        setContent(name);
    }

    public String getUserName() {
        if (name.getValue() == null
                || ((String) name.getValue()).trim().equals(""))
            return null;

        return ((String) name.getValue()).trim();
    }

    @Override
    public void attach() {
        super.attach();
        name.focus();
    }
}

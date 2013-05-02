package org.vaadin.artur.multiusercalendar.data;

import org.vaadin.artur.multiusercalendar.data.MUCEventProvider.MUCEventListener;

public interface MUCEventDispatcher {

    void dispatch(MUCEventListener listener);

}

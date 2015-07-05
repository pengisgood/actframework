package act.app.event;

import act.app.App;
import act.event.ActEvent;
import org.osgl.util.C;

import java.util.Map;

public abstract class AppEvent extends ActEvent<App> {
    private int id;
    public AppEvent(AppEventId id, App source) {
        super(source);
        this.id = id.ordinal();
    }
    public int id() {
        return id;
    }
}

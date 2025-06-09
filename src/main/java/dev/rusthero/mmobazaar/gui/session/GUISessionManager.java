package dev.rusthero.mmobazaar.gui.session;

import dev.rusthero.mmobazaar.gui.session.type.ConfirmGUISession;
import dev.rusthero.mmobazaar.gui.session.type.CustomerGUISession;
import dev.rusthero.mmobazaar.gui.session.type.OwnerGUISession;

public class GUISessionManager {
    public final CustomerGUISession customer = new CustomerGUISession();
    public final OwnerGUISession owner = new OwnerGUISession();
    public final ConfirmGUISession confirm = new ConfirmGUISession();
}

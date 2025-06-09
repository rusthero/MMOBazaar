package dev.rusthero.mmobazaar.gui.session;

public class GUISessionManager {
    public final CustomerGUISession customer = new CustomerGUISession();
    public final OwnerGUISession owner = new OwnerGUISession();
    public final ConfirmGUISession confirm = new ConfirmGUISession();
}

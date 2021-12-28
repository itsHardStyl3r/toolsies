package me.hardstyl3r.tchat.managers;

public class ChatManager {

    public ChatManager() {
    }

    public boolean chatLocked = false;

    public boolean isLocked() {
        return chatLocked;
    }

    public void toggleLocked() {
        chatLocked = !chatLocked;
    }
}

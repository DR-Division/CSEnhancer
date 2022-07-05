package com.division.message;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class CEMessage {

    private String message;
    private String originMessage;
    private int duration;

    public CEMessage(String message) {
        this.message = message;
        this.originMessage = message;
    }

    public CEMessage(String message, int duration) {
        this(message);
        this.duration = duration;
    }
    public String getMessage() {
        return this.message;
    }

    public int getDuration() {
        return this.duration;
    }

    public CEMessage resetOriginMessage() {
        message = originMessage;
        return this;
    }

    public CEMessage replace(String from, String to) {
        this.message = this.message.replace(from, to);
        return this;
    }

    public void send(Player p) {
        this.message = ChatColor.translateAlternateColorCodes('&', this.message);
        sendMessage(p);
    }

    public abstract void sendMessage(Player p);

}

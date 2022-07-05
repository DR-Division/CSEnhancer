package com.division.message.implement;

import com.division.message.CEMessage;
import org.bukkit.entity.Player;

public class ChatMessage extends CEMessage {
    public ChatMessage(String message) {
        super(message);
    }

    @Override
    public void sendMessage(Player p) {
        p.sendMessage(getMessage());
    }
}

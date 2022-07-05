package com.division.message.implement;

import com.division.message.CEMessage;
import org.bukkit.entity.Player;

public class TitleMessage extends CEMessage {
    public TitleMessage(String message, int duration) {
        super(message, duration);
    }

    @Override
    public void sendMessage(Player p) {
        String[] arr = getMessage().split("<->");
        if (arr.length == 2)
            p.sendTitle(arr[0], arr[1], 0, getDuration(), 0);
        else
            p.sendTitle("",getMessage(),0, getDuration(), 0);
    }
}

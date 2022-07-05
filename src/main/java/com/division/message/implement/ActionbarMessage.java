package com.division.message.implement;

import com.comphenix.packetwrapper.WrapperPlayServerChat;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.division.message.CEMessage;
import org.bukkit.entity.Player;

public class ActionbarMessage extends CEMessage {
    public ActionbarMessage(String message) {
        super(message);
    }

    @Override
    public void sendMessage(Player p) {
        WrapperPlayServerChat chat = new WrapperPlayServerChat();
        WrappedChatComponent component = WrappedChatComponent.fromText(getMessage());
        chat.setMessage(component);
        chat.setChatType(EnumWrappers.ChatType.GAME_INFO);
        chat.sendPacket(p);
    }
}

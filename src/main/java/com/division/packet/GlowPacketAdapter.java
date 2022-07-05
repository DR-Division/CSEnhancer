package com.division.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class GlowPacketAdapter extends PacketAdapter {

    public GlowPacketAdapter(Plugin plugin, PacketType... types) {
        super(plugin, types);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            Player p = event.getPlayer();
            if (p.hasMetadata("CE_Thermal")) {
                WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(event.getPacket().deepClone());
                Entity entity = metadata.getEntity(event);
                if (entity != p && entity instanceof LivingEntity) {
                    List<WrappedWatchableObject> metas = metadata.getMetadata();
                    Object object = metas.get(0).getValue();
                    if ((object instanceof Byte)) {
                        metadata.getMetadata().get(0).setValue((byte) ((byte) metas.get(0).getValue() | 0x40));
                    }
                    event.setPacket(metadata.getHandle());
                }
            }
        }

    }
}

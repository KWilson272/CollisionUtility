package me.kwilson272.collisionutility;

import com.projectkorra.projectkorra.event.BendingReloadEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ReloadListener implements Listener {

    private final CollisionUtility collisionUtility;

    public ReloadListener(CollisionUtility collisionUtility) {
        this.collisionUtility = collisionUtility;
    }

    @EventHandler
    private void onReload(BendingReloadEvent event) {
        collisionUtility.reload();
    }
}

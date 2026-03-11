package me.kristiyan.moviebot.listeners;

import me.kristiyan.moviebot.services.PremiumService;
import net.dv8tion.jda.api.events.entitlement.EntitlementCreateEvent;
import net.dv8tion.jda.api.events.entitlement.EntitlementUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EntitlementListener extends ListenerAdapter {

    private final PremiumService premiumService;

    public EntitlementListener(PremiumService premiumService) {
        this.premiumService = premiumService;
    }

    @Override
    public void onEntitlementCreate(EntitlementCreateEvent event) {
        premiumService.onEntitlementCreate(event.getEntitlement());
    }

    @Override
    public void onEntitlementUpdate(EntitlementUpdateEvent event) {
        premiumService.onEntitlementUpdate(event.getEntitlement());
    }
}
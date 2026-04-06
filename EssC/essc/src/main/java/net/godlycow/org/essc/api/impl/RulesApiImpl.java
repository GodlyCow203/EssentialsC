package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.RulesApi;
import net.godlycow.org.essc.api.rules.RuleEntry;
import net.godlycow.org.essc.rules.RulesManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RulesApiImpl implements RulesApi {

    private final RulesManager manager;

    public RulesApiImpl(RulesManager manager) {
        this.manager = manager;
    }

    @Override
    public List<RuleEntry> getRules() {
        AtomicInteger index = new AtomicInteger(0);
        return manager.getRules().stream()
                .map(c -> new RuleEntry(index.getAndIncrement(), PlainTextComponentSerializer.plainText().serialize(c)))
                .collect(Collectors.toList());
    }

    @Override
    public int getRuleCount() {
        return manager.getRuleCount();
    }

    @Override
    public void reload() {
        manager.reload();
    }
}
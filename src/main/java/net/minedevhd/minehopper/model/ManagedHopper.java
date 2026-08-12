package net.minedevhd.minehopper.model;

import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ManagedHopper {
    private final BlockKey source;
    private final List<BlockKey> targets;
    private int transferAmount;
    private boolean fastTick;
    private int collectRadius;
    private FilterMode filterMode;
    private ItemStack filterItem;
    private int routingCursor;

    public ManagedHopper(BlockKey source, int transferAmount, boolean fastTick, int collectRadius,
                         FilterMode filterMode, ItemStack filterItem, List<BlockKey> targets) {
        this.source = Objects.requireNonNull(source, "source");
        this.transferAmount = clampTransferAmount(transferAmount);
        this.fastTick = fastTick;
        this.collectRadius = Math.max(0, collectRadius);
        this.filterMode = filterMode == null ? FilterMode.OFF : filterMode;
        this.filterItem = filterItem == null ? null : filterItem.clone();
        this.targets = new ArrayList<>(targets == null ? List.of() : targets);
        if (this.filterItem == null) this.filterMode = FilterMode.OFF;
    }
    public BlockKey source() { return source; }
    public int transferAmount() { return transferAmount; }
    public void setTransferAmount(int amount) { transferAmount = clampTransferAmount(amount); }
    public boolean fastTick() { return fastTick; }
    public void setFastTick(boolean value) { fastTick = value; }
    public int collectRadius() { return collectRadius; }
    public void setCollectRadius(int radius) { collectRadius = Math.max(0, radius); }
    public FilterMode filterMode() { return filterMode; }
    public ItemStack filterItem() { return filterItem == null ? null : filterItem.clone(); }
    public void setFilter(ItemStack item, FilterMode mode) {
        if (item == null || item.getType().isAir() || mode == null || mode == FilterMode.OFF) { clearFilter(); return; }
        filterItem = item.clone(); filterItem.setAmount(1); filterMode = mode;
    }
    public void clearFilter() { filterMode = FilterMode.OFF; filterItem = null; }
    public boolean matchesFilter(ItemStack candidate) {
        if (candidate == null || candidate.getType().isAir()) return false;
        if (filterMode == FilterMode.OFF || filterItem == null) return true;
        if (filterMode == FilterMode.MATERIAL) return candidate.getType() == filterItem.getType();
        return candidate.isSimilar(filterItem);
    }
    public List<BlockKey> targets() { return Collections.unmodifiableList(targets); }
    public boolean addTarget(BlockKey target) {
        if (target == null || source.equals(target) || targets.contains(target)) return false;
        return targets.add(target);
    }
    public boolean removeTarget(BlockKey target) { boolean removed = targets.remove(target); normalizeCursor(); return removed; }
    public BlockKey removeTarget(int index) {
        if (index < 0 || index >= targets.size()) return null;
        BlockKey removed = targets.remove(index); normalizeCursor(); return removed;
    }
    public int routingCursor() { normalizeCursor(); return routingCursor; }
    public void advanceRoutingCursor(int usedIndex) {
        routingCursor = targets.isEmpty() ? 0 : Math.floorMod(usedIndex + 1, targets.size());
    }
    private void normalizeCursor() { routingCursor = targets.isEmpty() ? 0 : Math.floorMod(routingCursor, targets.size()); }
    private static int clampTransferAmount(int amount) { return Math.max(1, Math.min(64, amount)); }
}

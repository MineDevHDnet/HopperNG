package net.minedevhd.minehopper.service;

import net.minedevhd.minehopper.config.PluginSettings;
import net.minedevhd.minehopper.model.BlockKey;
import net.minedevhd.minehopper.model.FilterMode;
import net.minedevhd.minehopper.model.ManagedHopper;
import net.minedevhd.minehopper.persistence.HopperRepository;
import net.minedevhd.minehopper.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class HopperService {
    public enum LinkResult { SUCCESS, SOURCE_MISSING, NOT_CONTAINER, SELF, DUPLICATE, LIMIT_REACHED }
    public record Stats(int managed, int loaded, int fast, int targets) {}
    public record PruneResult(int removedSources, int removedTargets, int skippedUnloaded) {}
    private final JavaPlugin plugin;
    private final HopperRepository repository;
    private final Map<BlockKey, ManagedHopper> hoppers = new LinkedHashMap<>();
    private final Map<UUID, BlockKey> linkSessions = new HashMap<>();
    private final Set<BlockKey> targetIndex = new HashSet<>();
    private PluginSettings settings;
    private BukkitTask fastTask;
    private BukkitTask normalTask;

    public HopperService(JavaPlugin plugin, HopperRepository repository) {
        this.plugin = plugin; this.repository = repository; this.settings = PluginSettings.load(plugin);
    }
    public void loadAndStart() { hoppers.clear(); hoppers.putAll(repository.loadAll()); rebuildTargetIndex(); restartTasks(); plugin.getLogger().info("Loaded " + hoppers.size() + " managed hopper(s)."); }
    public void reload() { settings = PluginSettings.load(plugin); hoppers.clear(); hoppers.putAll(repository.loadAll()); linkSessions.clear(); rebuildTargetIndex(); restartTasks(); }
    public void shutdown() { cancelTasks(); linkSessions.clear(); persist(); }
    public PluginSettings settings() { return settings; }
    public boolean isManaged(Block block) { return block != null && isManaged(BlockKey.from(block)); }
    public boolean isManaged(BlockKey key) { return key != null && hoppers.containsKey(key); }
    public boolean isTrackedBlock(BlockKey key) { return key != null && (hoppers.containsKey(key) || targetIndex.contains(key)); }
    public ManagedHopper get(BlockKey key) { return hoppers.get(key); }

    public ManagedHopper register(Block block) {
        if (block == null || block.getType() != Material.HOPPER || !settings.isWorldEnabled(block.getWorld())) return null;
        BlockKey key = BlockKey.from(block); ManagedHopper existing = hoppers.get(key); if (existing != null) return existing;
        ManagedHopper hopper = new ManagedHopper(key, settings.defaultTransferAmount(), settings.defaultFastTick(), settings.defaultCollectRadius(), FilterMode.OFF, null, List.of());
        hoppers.put(key, hopper); persist(); return hopper;
    }
    public boolean unmanage(BlockKey source) {
        ManagedHopper removed = hoppers.remove(source); if (removed == null) return false;
        linkSessions.entrySet().removeIf(e -> source.equals(e.getValue())); rebuildTargetIndex(); persist(); return true;
    }
    public void handleBlockRemoved(BlockKey removed) {
        if (!isTrackedBlock(removed)) return;
        boolean changed = hoppers.remove(removed) != null;
        for (ManagedHopper hopper : hoppers.values()) changed |= hopper.removeTarget(removed);
        linkSessions.entrySet().removeIf(e -> removed.equals(e.getValue()));
        if (changed) { rebuildTargetIndex(); persist(); }
    }
    public boolean beginLink(Player player, BlockKey source) { if (player == null || source == null || !hoppers.containsKey(source)) return false; linkSessions.put(player.getUniqueId(), source); return true; }
    public boolean hasLinkSession(Player player) { return player != null && linkSessions.containsKey(player.getUniqueId()); }
    public boolean cancelLink(Player player) { return player != null && linkSessions.remove(player.getUniqueId()) != null; }
    public LinkResult completeLink(Player player, Block targetBlock) {
        if (player == null || targetBlock == null) return LinkResult.NOT_CONTAINER;
        BlockKey sourceKey = linkSessions.get(player.getUniqueId()); ManagedHopper hopper = hoppers.get(sourceKey);
        if (hopper == null) { linkSessions.remove(player.getUniqueId()); return LinkResult.SOURCE_MISSING; }
        if (!(targetBlock.getState() instanceof InventoryHolder)) return LinkResult.NOT_CONTAINER;
        BlockKey target = BlockKey.from(targetBlock);
        if (sourceKey.equals(target)) return LinkResult.SELF;
        if (hopper.targets().contains(target)) return LinkResult.DUPLICATE;
        if (hopper.targets().size() >= settings.maxTargetsPerHopper()) return LinkResult.LIMIT_REACHED;
        hopper.addTarget(target); linkSessions.remove(player.getUniqueId()); rebuildTargetIndex(); persist(); return LinkResult.SUCCESS;
    }
    public int cycleTransferAmount(BlockKey source) {
        ManagedHopper h = hoppers.get(source); if (h == null) return -1;
        int next = switch (h.transferAmount()) { case 1 -> 12; case 12 -> 64; default -> 1; };
        h.setTransferAmount(next); persist(); return next;
    }
    public Boolean toggleFastTick(BlockKey source) { ManagedHopper h = hoppers.get(source); if (h == null) return null; h.setFastTick(!h.fastTick()); persist(); return h.fastTick(); }
    public int adjustCollectRadius(BlockKey source, int delta) { ManagedHopper h = hoppers.get(source); if (h == null) return -1; int next = Math.max(0, Math.min(settings.maxCollectRadius(), h.collectRadius()+delta)); h.setCollectRadius(next); persist(); return next; }
    public boolean setFilter(BlockKey source, ItemStack sample) { ManagedHopper h = hoppers.get(source); if (h == null || sample == null || sample.getType().isAir()) return false; FilterMode mode = h.filterMode()==FilterMode.EXACT?FilterMode.EXACT:FilterMode.MATERIAL; h.setFilter(sample, mode); persist(); return true; }
    public boolean clearFilter(BlockKey source) { ManagedHopper h = hoppers.get(source); if (h == null) return false; h.clearFilter(); persist(); return true; }
    public FilterMode toggleFilterMode(BlockKey source) { ManagedHopper h=hoppers.get(source); if (h==null || h.filterItem()==null) return null; FilterMode next=h.filterMode()==FilterMode.EXACT?FilterMode.MATERIAL:FilterMode.EXACT; h.setFilter(h.filterItem(),next); persist(); return next; }
    public BlockKey removeTarget(BlockKey source, int index) { ManagedHopper h=hoppers.get(source); if(h==null)return null; BlockKey removed=h.removeTarget(index); if(removed!=null){rebuildTargetIndex();persist();} return removed; }

    public Stats stats() {
        int loaded=0, fast=0, targets=0;
        for(ManagedHopper h:hoppers.values()) { if(h.fastTick())fast++; targets+=h.targets().size(); World w=h.source().resolveWorld(); if(w==null||!h.source().isChunkLoaded())continue; if(w.getBlockAt(h.source().x(),h.source().y(),h.source().z()).getType()==Material.HOPPER)loaded++; }
        return new Stats(hoppers.size(),loaded,fast,targets);
    }
    public PruneResult pruneInvalidLoadedEntries() {
        int removedSources=0,removedTargets=0,skipped=0; boolean dirty=false;
        for(ManagedHopper h:new ArrayList<>(hoppers.values())) {
            BlockKey s=h.source(); World w=s.resolveWorld(); if(w==null||!s.isChunkLoaded()){skipped++;continue;}
            if(w.getBlockAt(s.x(),s.y(),s.z()).getType()!=Material.HOPPER){hoppers.remove(s);removedSources++;dirty=true;continue;}
            for(BlockKey t:new ArrayList<>(h.targets())) { World tw=t.resolveWorld(); if(tw==null||!t.isChunkLoaded()){skipped++;continue;} if(!(tw.getBlockAt(t.x(),t.y(),t.z()).getState() instanceof InventoryHolder)){if(h.removeTarget(t)){removedTargets++;dirty=true;}} }
        }
        if(dirty){rebuildTargetIndex();persist();} return new PruneResult(removedSources,removedTargets,skipped);
    }

    public void visualize(Player player, BlockKey sourceKey) {
        if(!hoppers.containsKey(sourceKey)){Messages.error(player,"Dieser Trichter wird nicht mehr von HopperNG verwaltet.");return;}
        int period=settings.visualizerPeriodTicks(); int maxRuns=Math.max(1,(settings.visualizerDurationSeconds()*20)/period);
        new BukkitRunnable(){int runs; @Override public void run(){ ManagedHopper h=hoppers.get(sourceKey); if(!player.isOnline()||h==null||runs++>=maxRuns){cancel();return;} World w=sourceKey.resolveWorld(); if(w==null||!sourceKey.isChunkLoaded()||!player.getWorld().equals(w)){cancel();return;} Location center=new Location(w,sourceKey.x()+0.5,sourceKey.y()+0.8,sourceKey.z()+0.5); drawRadius(player,center,h.collectRadius()); for(BlockKey t:h.targets()){World tw=t.resolveWorld(); if(tw==null||!tw.equals(w)||!t.isChunkLoaded())continue; drawLine(player,center,new Location(w,t.x()+0.5,t.y()+0.8,t.z()+0.5));}}}.runTaskTimer(plugin,0L,period);
    }

    private void restartTasks(){cancelTasks(); fastTask=Bukkit.getScheduler().runTaskTimer(plugin,()->processGroup(true),settings.fastIntervalTicks(),settings.fastIntervalTicks()); normalTask=Bukkit.getScheduler().runTaskTimer(plugin,()->processGroup(false),settings.normalIntervalTicks(),settings.normalIntervalTicks());}
    private void cancelTasks(){if(fastTask!=null){fastTask.cancel();fastTask=null;} if(normalTask!=null){normalTask.cancel();normalTask=null;}}
    private void processGroup(boolean fast){boolean dirty=false;List<BlockKey> invalidSources=new ArrayList<>();for(ManagedHopper h:new ArrayList<>(hoppers.values())){if(h.fastTick()!=fast)continue;ProcessOutcome o=processOne(h);if(o.sourceInvalid()){invalidSources.add(h.source());dirty=true;}for(BlockKey t:o.invalidTargets())if(h.removeTarget(t))dirty=true;}for(BlockKey s:invalidSources){hoppers.remove(s);linkSessions.entrySet().removeIf(e->s.equals(e.getValue()));}if(dirty){rebuildTargetIndex();persist();}}
    private ProcessOutcome processOne(ManagedHopper h){BlockKey s=h.source();World w=s.resolveWorld();if(w==null||!settings.isWorldEnabled(w)||!s.isChunkLoaded())return ProcessOutcome.OK;Block b=w.getBlockAt(s.x(),s.y(),s.z());if(b.getType()!=Material.HOPPER)return new ProcessOutcome(true,Set.of());if(b.isBlockPowered()||b.isBlockIndirectlyPowered())return ProcessOutcome.OK;BlockState st=b.getState();if(!(st instanceof Hopper hopper))return new ProcessOutcome(true,Set.of());Inventory inv=hopper.getInventory();int budget=h.transferAmount();int pulled=pullFromAbove(h,b,inv,budget);budget-=pulled;if(budget>0&&h.collectRadius()>0)collectNearbyItems(h,b,inv,budget);return new ProcessOutcome(false,pushToTargets(h,inv,h.transferAmount()));}
    private int pullFromAbove(ManagedHopper h,Block sourceBlock,Inventory destination,int budget){if(budget<=0)return 0;World w=sourceBlock.getWorld();int x=sourceBlock.getX(),y=sourceBlock.getY()+1,z=sourceBlock.getZ();if(!w.isChunkLoaded(x>>4,z>>4))return 0;BlockState st=w.getBlockAt(x,y,z).getState();if(!(st instanceof InventoryHolder holder))return 0;Inventory source=holder.getInventory();if(source==destination)return 0;int moved=0;for(int slot=0;slot<source.getSize()&&moved<budget;slot++){ItemStack item=source.getItem(slot);if(!h.matchesFilter(item))continue;moved+=moveFromSlot(source,slot,destination,budget-moved);}return moved;}
    private int collectNearbyItems(ManagedHopper h,Block sourceBlock,Inventory destination,int budget){if(budget<=0)return 0;int r=h.collectRadius();Location center=sourceBlock.getLocation().add(0.5,0.5,0.5);Collection<Entity> nearby=sourceBlock.getWorld().getNearbyEntities(center,r,r,r,e->e instanceof Item);List<Item> items=nearby.stream().filter(Item.class::isInstance).map(Item.class::cast).filter(i->i.isValid()&&!i.isDead()).sorted(Comparator.comparingDouble(i->i.getLocation().distanceSquared(center))).toList();int moved=0;for(Item entity:items){if(moved>=budget)break;ItemStack stack=entity.getItemStack();if(!h.matchesFilter(stack))continue;int wanted=Math.min(stack.getAmount(),budget-moved);ItemStack moving=stack.clone();moving.setAmount(wanted);int amount=insert(destination,moving);if(amount<=0)continue;int remaining=stack.getAmount()-amount;if(remaining<=0)entity.remove();else{ItemStack remainder=stack.clone();remainder.setAmount(remaining);entity.setItemStack(remainder);}moved+=amount;}return moved;}
    private Set<BlockKey> pushToTargets(ManagedHopper h,Inventory source,int budget){if(budget<=0||h.targets().isEmpty())return Set.of();Set<BlockKey> invalid=new HashSet<>();int size=h.targets().size(),start=h.routingCursor();for(int offset=0;offset<size;offset++){int index=Math.floorMod(start+offset,size);BlockKey t=h.targets().get(index);World w=t.resolveWorld();if(w==null||!t.isChunkLoaded())continue;BlockState st=w.getBlockAt(t.x(),t.y(),t.z()).getState();if(!(st instanceof InventoryHolder holder)){invalid.add(t);continue;}Inventory dest=holder.getInventory();if(dest==source){invalid.add(t);continue;}int moved=moveInventory(source,dest,budget);if(moved>0){h.advanceRoutingCursor(index);break;}}return invalid;}
    private int moveInventory(Inventory source,Inventory dest,int budget){int moved=0;for(int slot=0;slot<source.getSize()&&moved<budget;slot++){ItemStack item=source.getItem(slot);if(item==null||item.getType().isAir())continue;moved+=moveFromSlot(source,slot,dest,budget-moved);}return moved;}
    private int moveFromSlot(Inventory source,int slot,Inventory dest,int budget){ItemStack stack=source.getItem(slot);if(stack==null||stack.getType().isAir()||budget<=0)return 0;int wanted=Math.min(stack.getAmount(),budget);ItemStack moving=stack.clone();moving.setAmount(wanted);int moved=insert(dest,moving);if(moved<=0)return 0;int remaining=stack.getAmount()-moved;if(remaining<=0)source.setItem(slot,null);else{ItemStack remainder=stack.clone();remainder.setAmount(remaining);source.setItem(slot,remainder);}return moved;}
    private int insert(Inventory dest,ItemStack moving){int requested=moving.getAmount();Map<Integer,ItemStack> leftovers=dest.addItem(moving);int leftover=leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();return requested-leftover;}
    private void drawRadius(Player player,Location center,int radius){if(radius<=0){player.spawnParticle(Particle.END_ROD,center,1,0,0,0,0);return;}int points=Math.max(16,Math.min(64,radius*8));for(int i=0;i<points;i++){double a=Math.PI*2.0*i/points;player.spawnParticle(Particle.END_ROD,center.clone().add(Math.cos(a)*radius,0,Math.sin(a)*radius),1,0,0,0,0);}}
    private void drawLine(Player player,Location from,Location to){double distance=from.distance(to);int steps=Math.max(2,Math.min(80,(int)Math.ceil(distance*2.0)));double dx=to.getX()-from.getX(),dy=to.getY()-from.getY(),dz=to.getZ()-from.getZ();for(int i=0;i<=steps;i++){double t=i/(double)steps;player.spawnParticle(Particle.END_ROD,from.clone().add(dx*t,dy*t,dz*t),1,0,0,0,0);}}
    private void rebuildTargetIndex(){targetIndex.clear();for(ManagedHopper h:hoppers.values())targetIndex.addAll(h.targets());}
    private void persist(){repository.saveAll(hoppers.values());}
    private record ProcessOutcome(boolean sourceInvalid,Set<BlockKey> invalidTargets){private static final ProcessOutcome OK=new ProcessOutcome(false,Set.of());}
}

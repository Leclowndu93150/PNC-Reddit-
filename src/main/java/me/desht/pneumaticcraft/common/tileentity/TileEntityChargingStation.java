package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.item.IChargingStationGUIHolderItem;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.GlobalTileEntityCacheManager;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TileEntityChargingStation extends TileEntityPneumaticBase implements IRedstoneControl, ICamouflageableTE {
    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.button.never",
            "gui.tab.redstoneBehaviour.chargingStation.button.doneDischarging",
            "gui.tab.redstoneBehaviour.chargingStation.button.charging",
            "gui.tab.redstoneBehaviour.chargingStation.button.discharging"
    );

    @DescSynced
    private ChargingStationHandler inventory;
    private ChargeableItemHandler chargeableInventory;

    private static final int INVENTORY_SIZE = 1;

    public static final int CHARGE_INVENTORY_INDEX = 0;
    private static final float ANIMATION_AIR_SPEED = 0.001F;

    @GuiSynced
    public float chargingItemPressure;
    @GuiSynced
    public boolean charging;
    @GuiSynced
    public boolean discharging;
    @GuiSynced
    public int redstoneMode;
    private boolean oldRedstoneStatus;
    @DescSynced
    private ItemStack camoStack = ItemStack.EMPTY;
    private IBlockState camoState;

    public TileEntityChargingStation() {
        super(PneumaticValues.DANGER_PRESSURE_CHARGING_STATION, PneumaticValues.MAX_PRESSURE_CHARGING_STATION, PneumaticValues.VOLUME_CHARGING_STATION, 4);
        inventory = new ChargingStationHandler();
        addApplicableUpgrade(EnumUpgrade.SPEED, EnumUpgrade.DISPENSER);
    }

    @Override
    public void onDescUpdate() {
        camoState = ICamouflageableTE.getStateForStack(camoStack);
        getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
    }

    @Nonnull
    public ItemStack getChargingItem() {
        return inventory.getStackInSlot(CHARGE_INVENTORY_INDEX);
    }

    @Override
    public void update() {
        super.update();

        if (!world.isRemote) {
            discharging = false;
            charging = false;


            int airToTransfer = (int) (PneumaticValues.CHARGING_STATION_CHARGE_RATE * getSpeedMultiplierFromUpgrades());

            List<Pair<IPressurizable, ItemStack>> l = findChargeableItems();
            for (int i = 0; i < l.size() && airHandler.getAir() > 0; i++) {
                IPressurizable p = l.get(i).getLeft();
                ItemStack chargingStack = l.get(i).getRight();

                float itemPressure = p.getPressure(chargingStack);
                float itemVolume = p.getVolume(chargingStack);
                float delta = Math.abs(getPressure() - itemPressure) / 2.0F;
                int airInItem = (int) (itemPressure * itemVolume);

                if (itemPressure > getPressure() + 0.01F && itemPressure > 0F) {
                    // move air from item to charger
                    int airToMove = Math.min(Math.min(airToTransfer, airInItem), (int) (delta * airHandler.getVolume()));
                    p.addAir(chargingStack, -airToMove);
                    this.addAir(airToMove);
                    discharging = true;
                } else if (itemPressure < getPressure() - 0.01F && itemPressure < p.maxPressure(chargingStack)) {
                    // move air from charger to item
                    int maxAirInItem = (int) (p.maxPressure(chargingStack) * itemVolume);
                    int airToMove = Math.min(Math.min(airToTransfer, airHandler.getAir()), maxAirInItem - airInItem);
                    airToMove = Math.min((int) (delta * itemVolume), airToMove);
                    p.addAir(chargingStack, airToMove);
                    this.addAir(-airToMove);
                    charging = true;
                }
            }

            if (oldRedstoneStatus != shouldEmitRedstone()) {
                oldRedstoneStatus = shouldEmitRedstone();
                updateNeighbours();
            }

            List<Pair<EnumFacing, IAirHandler>> teList = getAirHandler(null).getConnectedPneumatics();
            if (teList.size() == 0) {
                getAirHandler(null).airLeak(getRotation());
            }
        }
    }

    private List<Pair<IPressurizable, ItemStack>> findChargeableItems() {
        List<Pair<IPressurizable, ItemStack>> res = new ArrayList<>();

        if (getChargingItem().getItem() instanceof IPressurizable) {
            IPressurizable p = IPressurizable.of(getChargingItem());
            res.add(Pair.of(p, getChargingItem()));
            chargingItemPressure = p.getPressure(getChargingItem());
        }

        if (getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            // creating a new word, 'entities padding'!
            List<Entity> entitiesPadding = getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(getPos().up()));
            for (Entity entity : entitiesPadding) {
                if (entity instanceof IPressurizable) {
                    res.add(Pair.of((IPressurizable) entity, ItemStack.EMPTY));
                } else if (entity instanceof EntityItem) {
                    ItemStack entityStack = ((EntityItem) entity).getItem();
                    if (entityStack.getItem() instanceof IPressurizable) {
                        res.add(Pair.of(IPressurizable.of(entityStack), entityStack));
                    }
                } else if (entity instanceof EntityPlayer) {
                    InventoryPlayer inv = ((EntityPlayer) entity).inventory;
                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (stack.getItem() instanceof IPressurizable) {
                            res.add(Pair.of(IPressurizable.of(stack), stack));
                        }
                    }
                }
            }
        }

        return res;
    }

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return getRotation() == side;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 3) redstoneMode = 0;
            updateNeighbours();
        } else if ((buttonID == 1 || buttonID == 2) && getChargingItem().getItem() instanceof IChargingStationGUIHolderItem) {
            player.openGui(PneumaticCraftRepressurized.instance,
                    buttonID == 1 ?
                            ((IChargingStationGUIHolderItem) getChargingItem().getItem()).getGuiID().ordinal() :
                            EnumGuiId.CHARGING_STATION.ordinal(),
                    getWorld(), getPos().getX(), getPos().getY(), getPos().getZ());
        }
    }

    public boolean shouldEmitRedstone() {
        switch (redstoneMode) {
            case 0:
                return false;
            case 1:
                return !charging && !discharging && getChargingItem().getItem() instanceof IPressurizable;
            case 2:
                return charging;
            case 3:
                return discharging;

        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

    public ChargeableItemHandler getChargeableInventory() {
        return getWorld().isRemote ? new ChargeableItemHandler(this) : chargeableInventory;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public String getName() {
        return Blockss.CHARGING_STATION.getTranslationKey();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        redstoneMode = tag.getInteger("redstoneMode");
        inventory = new ChargingStationHandler();
        inventory.deserializeNBT(tag.getCompoundTag("Items"));

        ItemStack chargeSlot = getChargingItem();
        if (chargeSlot.getItem() instanceof IChargingStationGUIHolderItem) {
            chargeableInventory = new ChargeableItemHandler(this);
        }

        camoStack = ICamouflageableTE.readCamoStackFromNBT(tag);
        camoState = ICamouflageableTE.getStateForStack(camoStack);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (chargeableInventory != null) {
            chargeableInventory.writeToNBT();
        }
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setTag("Items", inventory.serializeNBT());
        ICamouflageableTE.writeCamoStackToNBT(camoStack, tag);
        return tag;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (chargeableInventory != null) {
            chargeableInventory.writeToNBT();
        }
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public IBlockState getCamouflage() {
        return camoState;
    }

    @Override
    public void setCamouflage(IBlockState state) {
        camoState = state;
        camoStack = ICamouflageableTE.getStackForState(state);
        sendDescriptionPacket();
        markDirty();
    }
    
    @Override
    public void invalidate(){
        super.invalidate();
        GlobalTileEntityCacheManager.getInstance().chargingStations.remove(this);
    }
    
    @Override
    public void validate(){
        super.validate();
        GlobalTileEntityCacheManager.getInstance().chargingStations.add(this);
    }

    private class ChargingStationHandler extends FilteredItemStackHandler {
        ChargingStationHandler() {
            super(TileEntityChargingStation.this, INVENTORY_SIZE);
        }
        
        @Override
        public int getSlotLimit(int slot){
            return 1;
        }

        @Override
        public boolean test(Integer slot, ItemStack itemStack) {
            return slot == CHARGE_INVENTORY_INDEX && (itemStack.isEmpty() || itemStack.getItem() instanceof IPressurizable);
        }

        @Override
        protected void onContentsChanged(int slot) {
            TileEntityChargingStation teCS = TileEntityChargingStation.this;

            if (teCS.getWorld().isRemote || slot != CHARGE_INVENTORY_INDEX) return;

            teCS.chargeableInventory = getStackInSlot(slot).getItem() instanceof IChargingStationGUIHolderItem ?
                    new ChargeableItemHandler(teCS) :
                    null;

            // if any other player has a gui open for the previous item, force a reopen of the charging station gui
            for (EntityPlayer player : teCS.getWorld().playerEntities) {
                if (player.openContainer instanceof ContainerChargingStationItemInventory && ((ContainerChargingStationItemInventory) player.openContainer).te == te) {
                    player.openGui(PneumaticCraftRepressurized.instance, EnumGuiId.CHARGING_STATION.ordinal(), teCS.getWorld(), teCS.getPos().getX(), teCS.getPos().getY(), teCS.getPos().getZ());
                }
            }
        }
    }
}

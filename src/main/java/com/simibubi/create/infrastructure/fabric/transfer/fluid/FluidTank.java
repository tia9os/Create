package com.simibubi.create.infrastructure.fabric.transfer.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class FluidTank extends SingleFluidStorage {
	private long capacity;

	public FluidTank(long capacity) {
		this.capacity = capacity;
	}

	public void setCapacity(long capacity) {
		this.capacity = capacity;
	}

	@Override
	protected long getCapacity(FluidVariant variant) {
		return this.capacity;
	}

	public FluidStack getFluid() {
		return new FluidStack(this.variant, this.amount);
	}

	public void setFluid(FluidStack stack) {
		this.variant = stack.getVariant();
		this.amount = stack.getAmount();
	}

	public long getFluidAmount() {
		return this.amount;
	}

	public boolean isEmpty() {
		return this.variant.isBlank() || this.amount <= 0;
	}

	protected void onContentsChanged() {
	}

	@Override
	protected void onFinalCommit() {
		super.onFinalCommit();
		this.onContentsChanged();
	}

	public void readFromNBT(HolderLookup.Provider registries, CompoundTag nbt) {
		FluidStack parsed = FluidStack.parseOptional(registries, nbt);
		if (this.capacity > 0 && parsed.getAmount() > this.capacity) {
			parsed = parsed.copyWithAmount(this.capacity);
		}
		this.setFluid(parsed);
	}

	public CompoundTag writeToNBT(HolderLookup.Provider registries, CompoundTag nbt) {
		FluidStack fluid = this.getFluid();
		if (!fluid.isEmpty()) {
			nbt.put("Fluid", fluid.save(registries));
		}
		return nbt;
	}

	public void clamp() {
		if (this.amount > this.capacity) {
			this.amount = this.capacity;
		}
	}
}

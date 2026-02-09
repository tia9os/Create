package com.simibubi.create.content.fluids.transfer;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;

import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.MutableContainerItemContext;
import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;

public class GenericItemEmptying {

	public static boolean canItemBeEmptied(Level world, ItemStack stack) {
		if (PotionFluidHandler.isPotionItem(stack))
			return true;

		if (AllRecipeTypes.EMPTYING.find(new SingleRecipeInput(stack), world)
			.isPresent())
			return true;

		return TransferUtil.getFluidContained(stack).isPresent();
	}

	public static Pair<FluidStack, ItemStack> emptyItem(Level world, ItemStack stack, boolean simulate) {
		return emptyItem(world, stack, simulate, null);
	}

	public static Pair<FluidStack, ItemStack> emptyItem(Level world, ItemStack stack, boolean simulate, TransactionContext transaction) {
		FluidStack resultingFluid = FluidStack.EMPTY;
		ItemStack resultingItem = ItemStack.EMPTY;

		if (PotionFluidHandler.isPotionItem(stack))
			return PotionFluidHandler.emptyPotion(stack, simulate);

		Optional<RecipeHolder<Recipe<SingleRecipeInput>>> recipe = AllRecipeTypes.EMPTYING.find(new SingleRecipeInput(stack), world);
		if (recipe.isPresent()) {
			EmptyingRecipe emptyingRecipe = (EmptyingRecipe) recipe.get().value();
			List<ItemStack> results = emptyingRecipe.rollResults();
			if (!simulate)
				stack.shrink(1);
			resultingItem = results.isEmpty() ? ItemStack.EMPTY : results.get(0);
			resultingFluid = emptyingRecipe.getResultingFluid();
			return Pair.of(resultingFluid, resultingItem);
		}

		ItemStack split = stack.copy();
		split.setCount(1);
		MutableContainerItemContext ctx = new MutableContainerItemContext(split);
		Storage<FluidVariant> tank = FluidStorage.ITEM.find(split, ctx);
		if (tank == null)
			return Pair.of(resultingFluid, resultingItem);
		if (transaction != null) {
			if (simulate) {
				try (Transaction nested = transaction.openNested()) {
					resultingFluid = TransferUtil.extractAnyFluid(tank, FluidConstants.BUCKET, nested);
					int amount = ctx.getItemVariant().isBlank() ? 0 : (int) ctx.getAmount(); // GH#1622
					resultingItem = ctx.getItemVariant().toStack(amount);
				}
			} else {
				resultingFluid = TransferUtil.extractAnyFluid(tank, FluidConstants.BUCKET, transaction);
				int amount = ctx.getItemVariant().isBlank() ? 0 : (int) ctx.getAmount(); // GH#1622
				resultingItem = ctx.getItemVariant().toStack(amount);
				stack.shrink(1);
			}

			return Pair.of(resultingFluid, resultingItem);
		}
		try (Transaction t = Transaction.openOuter()) {
			resultingFluid = TransferUtil.extractAnyFluid(tank, FluidConstants.BUCKET, t);
			int amount = ctx.getItemVariant().isBlank() ? 0 : (int) ctx.getAmount(); // GH#1622
			resultingItem = ctx.getItemVariant().toStack(amount);
			if (!simulate) {
				stack.shrink(1);
				t.commit();
			}

			return Pair.of(resultingFluid, resultingItem);
		}
	}

}

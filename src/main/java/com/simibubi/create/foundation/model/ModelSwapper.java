package com.simibubi.create.foundation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.foundation.block.render.CustomBlockModels;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItems;
import com.simibubi.create.foundation.item.render.CustomItemModels;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.AfterBake;

public class ModelSwapper implements AfterBake {
	protected CustomBlockModels customBlockModels = new CustomBlockModels();
	protected CustomItemModels customItemModels = new CustomItemModels();
	private Map<Object, NonNullFunction<BakedModel, ? extends BakedModel>> swaps = null;
	private Map<ResourceLocation, NonNullFunction<BakedModel, ? extends BakedModel>> blockSwaps = null;

	public CustomBlockModels getCustomBlockModels() {
		return customBlockModels;
	}

	public CustomItemModels getCustomItemModels() {
		return customItemModels;
	}

	public void registerListeners() {
		ModelLoadingPlugin.register(ctx -> ctx.modifyModelAfterBake().register(this));
	}

	@Override
	public BakedModel modifyModelAfterBake(BakedModel model, Context context) {
		if (swaps == null)
			collectSwaps();

		Object modelId = context.resourceId();
		if (modelId == null)
			modelId = context.topLevelId();

		if (modelId == null)
			return model;

		NonNullFunction<BakedModel, ? extends BakedModel> swap = swaps.get(modelId);
		if (swap == null) {
			ModelResourceLocation topLevelId = context.topLevelId();
			if (topLevelId != null && !"inventory".equals(topLevelId.variant()))
				swap = blockSwaps.get(topLevelId.id());
		}

		if (swap != null) {
			try {
				model = swap.apply(model);
			} catch (ClassCastException ignored) {
				// Keep the original model if the swap expects a more specific type.
			}
		}

		return model;
	}

	private void collectSwaps() {
		Map<Object, NonNullFunction<BakedModel, ? extends BakedModel>> collectedSwaps = new HashMap<>();
		Map<ResourceLocation, NonNullFunction<BakedModel, ? extends BakedModel>> collectedBlockSwaps = new HashMap<>();
		customBlockModels.forEach((block, swapper) ->
			getAllBlockStateModelLocations(block).forEach(id -> collectedSwaps.put(id, swapper)));
		customBlockModels.forEach((block, swapper) -> collectedBlockSwaps.put(RegisteredObjectsHelper.getKeyOrThrow(block), swapper));
		customItemModels.forEach((item, swapper) -> collectedSwaps.put(getItemModelLocation(item), swapper));
		CustomRenderedItems.forEach(item -> collectedSwaps.put(getItemModelLocation(item), CustomRenderedItemModel::new));
		swaps = collectedSwaps;
		blockSwaps = collectedBlockSwaps;
	}

	public static List<ModelResourceLocation> getAllBlockStateModelLocations(Block block) {
		List<ModelResourceLocation> models = new ArrayList<>();
		ResourceLocation blockRl = RegisteredObjectsHelper.getKeyOrThrow(block);
		block.getStateDefinition()
			.getPossibleStates()
			.forEach(state -> {
				models.add(BlockModelShaper.stateToModelLocation(blockRl, state));
			});
		return models;
	}

	public static ModelResourceLocation getItemModelLocation(Item item) {
		return new ModelResourceLocation(RegisteredObjectsHelper.getKeyOrThrow(item), "inventory");
	}

}

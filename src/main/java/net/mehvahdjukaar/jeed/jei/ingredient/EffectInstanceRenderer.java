package net.mehvahdjukaar.jeed.jei.ingredient;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.mehvahdjukaar.jeed.Jeed;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EffectInstanceRenderer implements IIngredientRenderer<MobEffectInstance> {

    public static final EffectInstanceRenderer INSTANCE = new EffectInstanceRenderer(true);

    public static final EffectInstanceRenderer INSTANCE_SLOT = new EffectInstanceRenderer(false);

    private final Minecraft MC;

    private final boolean offset;

    public EffectInstanceRenderer(boolean offset) {
        MC = Minecraft.getInstance();
        this.offset = offset;
    }

    @Override
    public void render(PoseStack matrixStack, int xPosition, int yPosition, @Nullable MobEffectInstance effectInstance) {

        if (effectInstance != null ) {

            MobEffect effect = effectInstance.getEffect();


            MobEffectTextureManager potionspriteuploader = MC.getMobEffectTextures();
            TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effect);


            RenderSystem.clearColor(1.0F, 1.0F,1.0F,1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
            int o = offset ? -1 : (Jeed.REI ? 3 : 0);
            GuiComponent.blit(matrixStack, xPosition + o, yPosition+ o, 0, 18, 18, textureatlassprite);

            RenderSystem.applyModelViewMatrix();

        }
    }


    @Override
    public List<Component> getTooltip(MobEffectInstance effectInstance, TooltipFlag tooltipFlag) {
        return this.getTooltipsWithDescription(effectInstance, tooltipFlag, false);
    }

    public List<Component> getTooltipsWithDescription(MobEffectInstance effectInstance, TooltipFlag tooltipFlag, boolean reactsToShift) {
        List<Component> tooltip = new ArrayList<>();
        if (effectInstance != null) {

            MobEffect effect = effectInstance.getEffect();

            String name = I18n.get(effect.getDescriptionId());
            int amp = effectInstance.getAmplifier();
            if (amp >= 1 && amp <= 9) {
                name = name + ' ' + I18n.get("enchantment.level." + (amp + 1));
            }

            tooltip.add(new TextComponent(name));

            TextComponent colorValue = new TextComponent("#" + Integer.toHexString(effect.getColor()));
            colorValue.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(effect.getColor())));

            MutableComponent color = new TranslatableComponent("jeed.tooltip.color").withStyle(ChatFormatting.GRAY);

            tooltip.add(new TranslatableComponent("jeed.tooltip.color_complete", color, colorValue));
            if (effect.isBeneficial()) {
                tooltip.add(new TranslatableComponent("jeed.tooltip.beneficial").withStyle(ChatFormatting.BLUE));
            } else {
                tooltip.add(new TranslatableComponent("jeed.tooltip.harmful").withStyle(ChatFormatting.RED));
            }


            boolean showDescription = reactsToShift && Screen.hasShiftDown();
            //show full description with shift
            if(showDescription){
                ResourceLocation res = effect.getRegistryName();
                tooltip.add(new TranslatableComponent("effect." + res.getNamespace() + "." +
                        res.getPath() + ".description").withStyle(ChatFormatting.GRAY));
            }
            else {

                List<Pair<Attribute, AttributeModifier>> list1 = Lists.newArrayList();
                Map<Attribute, AttributeModifier> map = effect.getAttributeModifiers();
                if (!map.isEmpty()) {
                    for (Map.Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                        AttributeModifier attributemodifier = entry.getValue();
                        AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), effect.getAttributeModifierValue(effectInstance.getAmplifier(), attributemodifier), attributemodifier.getOperation());
                        list1.add(new Pair<>(entry.getKey(), attributemodifier1));
                    }
                }
                if (!list1.isEmpty()) {
                    tooltip.add(TextComponent.EMPTY);
                    tooltip.add((new TranslatableComponent("potion.whenDrank")).withStyle(ChatFormatting.DARK_PURPLE));

                    for (Pair<Attribute, AttributeModifier> pair : list1) {
                        AttributeModifier attributemodifier2 = pair.getSecond();
                        double d0 = attributemodifier2.getAmount();
                        double d1;
                        if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                            d1 = attributemodifier2.getAmount();
                        } else {
                            d1 = attributemodifier2.getAmount() * 100.0D;
                        }

                        if (d0 > 0.0D) {
                            tooltip.add((new TranslatableComponent("attribute.modifier.plus." + attributemodifier2.getOperation().toValue(),
                                    ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(pair.getFirst().getDescriptionId()))).withStyle(ChatFormatting.BLUE));
                        } else if (d0 < 0.0D) {
                            d1 = d1 * -1.0D;
                            tooltip.add((new TranslatableComponent("attribute.modifier.take." + attributemodifier2.getOperation().toValue(),
                                    ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(pair.getFirst().getDescriptionId()))).withStyle(ChatFormatting.RED));
                        }
                    }
                }
            }

            if (tooltipFlag.isAdvanced()) {
                tooltip.add(new TextComponent(effect.getRegistryName().toString()).withStyle(ChatFormatting.DARK_GRAY));
            }

        }
        return tooltip;
    }


}

package net.hexnowloading.hexfortress.item;

import net.hexnowloading.hexfortress.registry.HFItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlazeRodSwordItem extends SwordItem {

    public BlazeRodSwordItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack itemStack, Enchantment enchantment) {
        return enchantment != Enchantments.FIRE_ASPECT && super.canApplyAtEnchantingTable(itemStack, enchantment);
    }

    @Override
    public boolean isBookEnchantable(ItemStack itemStack, ItemStack book) {
        return !EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.FIRE_ASPECT) && super.isBookEnchantable(itemStack, book);
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(itemStack, target, attacker);
        if (result && !target.level().isClientSide() && !target.fireImmune()) {
            target.setSecondsOnFire(12);
        }
        return result;
    }

    @Override
    public boolean isValidRepairItem(ItemStack repairItem, ItemStack itemStack) {
        return itemStack.is(HFItems.BLAZE_CORE.get());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
}

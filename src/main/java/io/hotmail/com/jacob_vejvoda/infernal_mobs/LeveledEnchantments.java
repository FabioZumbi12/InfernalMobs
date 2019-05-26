package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import org.bukkit.enchantments.Enchantment;

class LevelledEnchantment {
    public Enchantment getEnchantment;
    public int getLevel;

    LevelledEnchantment(Enchantment enchantment, int level) {
        this.getEnchantment = enchantment;
        this.getLevel = level;
    }
}

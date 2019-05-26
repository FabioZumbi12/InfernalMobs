package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.UUID;

class Mob {
    public Entity entity;
    public UUID id;
    public World world;
    public boolean infernal;
    public int lives;
    public String effect;
    public ArrayList<String> abilityList = new ArrayList<String>();

    Mob(Entity type, UUID i, World w, boolean in, ArrayList<String> l, int li, String e) {
        this.entity = type;
        this.id = i;
        this.world = w;
        this.infernal = in;
        this.abilityList = l;
        this.lives = li;
        this.effect = e;
    }

    @SuppressWarnings("deprecation")
    public String toString() {
        return "Name: " + this.entity.getType().getName() + " Infernal: " + this.infernal + "Abilities:" + this.abilityList;
    }

    public void setLives(int i) {
        this.lives = i;
    }
}
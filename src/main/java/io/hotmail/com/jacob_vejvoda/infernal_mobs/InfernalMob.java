package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.UUID;

class InfernalMob {
    private boolean infernal;
    Entity entity;
    UUID id;
    int lives;
    String effect;
    ArrayList<String> abilityList;

    InfernalMob(Entity type, UUID i, boolean in, ArrayList<String> l, int li, String e) {
        this.entity = type;
        this.id = i;
        this.infernal = in;
        this.abilityList = l;
        this.lives = li;
        this.effect = e;
    }

    public String toString() {
        return "Name: " + this.entity.getType().name() + " Infernal: " + this.infernal + "Abilities:" + this.abilityList;
    }

    void setLives(int i) {
        this.lives = i;
    }
}
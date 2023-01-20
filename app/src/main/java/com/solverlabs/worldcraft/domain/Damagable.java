package com.solverlabs.worldcraft.domain;

public interface Damagable {
    boolean isDead();

    void takeDamage(int i);
}

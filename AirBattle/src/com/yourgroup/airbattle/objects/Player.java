package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;
import com.yourgroup.airbattle.input.InputHandler;
import com.yourgroup.airbattle.core.ObjectType;

public class Player extends GameObject {

    private int hp = 3;
    private double speed = 220; // px/s
    private double fireCooldown = 0.25;
    private double fireTimer = 0;
    private double fireBoostTimer = 0;
    private final double baseFireCooldown = 0.25;
    private final double  baseSpeed = 220;
    private double speedBoostTimer = 0;
    private final InputHandler input;


    public Player(double x, double y, Image sprite, InputHandler input) {
    super(x, y, 40, 40, sprite);
    this.input = input;
}

    @Override
    public ObjectType getType() {
        return ObjectType.PLAYER;
    }

    @Override
    public void update(double dt) {
    fireTimer = Math.max(0, fireTimer - dt);

    if (speedBoostTimer > 0) {
        speedBoostTimer -= dt;
        if (speedBoostTimer <= 0) {
            speed = baseSpeed;
        }
    }
    
    if (fireBoostTimer > 0) {
        fireBoostTimer -= dt;
        if (fireBoostTimer <= 0) {
            fireCooldown = baseFireCooldown;
        }
    }

    if (input.left())  moveLeft(dt);
    if (input.right()) moveRight(dt);
    if (input.up())    moveUp(dt);
    if (input.down())  moveDown(dt);
    
    }
    
    public void clampTo(double maxWidth, double maxHeight) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > maxWidth) {
        x = maxWidth - width;
        }
        if (y + height > maxHeight) {
        y = maxHeight - height;
        }
    }
    
    public void moveUp(double dt) {
    y -= speed * dt;
    }
    public void moveDown(double dt) {
    y += speed * dt;
    }
    public void moveLeft(double dt) {
        x -= speed * dt;
    }

    public void moveRight(double dt) {
        x += speed * dt;
    }

    public boolean canFire() {
        return fireTimer <= 0;
    }
    
    public void heal(int amount) {
    hp = Math.min(hp + amount, 5); // max HP = 5
    }

    public Bullet fire(Image bulletSprite) {
    if (!canFire()) return null;

    fireTimer = fireCooldown;
    return new Bullet(x + width / 2 - 3, y - 12, bulletSprite);
    }

    public void damage() {
        hp--;
        if (hp <= 0) kill();
    }
    
    public void boostSpeed(double bonus, double duration) {
    speed = baseSpeed + bonus;
    speedBoostTimer = duration;
    }
    
    public void boostFireRate(double cooldown, double duration) {
    fireCooldown = Math.min(fireCooldown, cooldown);
    fireBoostTimer = duration;
}


    public int getHp() {
        return hp;
    }
    public boolean isFiringPressed() {
        return input.fire();
    }

}
    


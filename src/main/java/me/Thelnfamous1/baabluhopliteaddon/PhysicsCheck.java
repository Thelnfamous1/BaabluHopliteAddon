package me.Thelnfamous1.baabluhopliteaddon;

public interface PhysicsCheck {

    default boolean canBypassGravity(){
        return false;
    }

}

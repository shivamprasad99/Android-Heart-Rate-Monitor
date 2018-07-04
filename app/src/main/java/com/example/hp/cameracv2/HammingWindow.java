package com.example.hp.cameracv2;

public class HammingWindow extends Window {
    /** Constructs a Hamming window. */
    public HammingWindow() {
    }

    protected float value(int length, int index) {
        return 0.54f - 0.46f * (float) Math.cos(TWO_PI * index / (length - 1));
    }
}
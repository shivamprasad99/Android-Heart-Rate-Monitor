package com.example.hp.cameracv2;

public abstract class Window {

    /** The float value of 2*PI. Provided as a convenience for subclasses. */
    protected static final float TWO_PI = (float) (2 * Math.PI);
    protected int length;

    public Window() {
    }

    /**
     * Apply the window function to a sample buffer.
     *
     * @param samples
     *            a sample buffer
     */
    public void apply(float[] samples) {
        this.length = samples.length;

        for (int n = 0; n < samples.length; n++) {
            samples[n] *= value(samples.length, n);
        }
    }

    /**
     * Generates the curve of the window function.
     *
     * @param length
     *            the length of the window
     * @return the shape of the window function
     */
    public float[] generateCurve(int length) {
        float[] samples = new float[length];
        for (int n = 0; n < length; n++) {
            samples[n] = 1f * value(length, n);
        }
        return samples;
    }

    protected abstract float value(int length, int index);
}

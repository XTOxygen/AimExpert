package com.endwithme.aimexpert.gui.widget;

/** Functional bindings so widgets stay independent from the settings class. */
public final class Settings
{
    public interface BoolGet
    {
        boolean get();
    }

    public interface BoolSet
    {
        void set(boolean value);
    }

    public interface IntGet
    {
        int get();
    }

    public interface IntSet
    {
        void set(int value);
    }

    public interface TextGet
    {
        String get();
    }

    public interface TextSet
    {
        void set(String value);
    }

    private Settings()
    {
    }
}

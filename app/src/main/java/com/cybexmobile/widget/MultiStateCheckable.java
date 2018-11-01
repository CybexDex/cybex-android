package com.cybexmobile.widget;

public interface MultiStateCheckable{

    /**
     * Change the checked state of the view
     *
     * @param checked The new checked state
     * @param state The checked up or down state
     */
    void setChecked(boolean checked, int state);

    /**
     * @return The current checked state of the view
     */
    boolean isChecked();

    /**
     * Change the checked state of the view to the inverse of its current state
     *
     */
    void toggle();

}

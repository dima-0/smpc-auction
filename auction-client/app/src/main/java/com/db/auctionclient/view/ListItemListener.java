package com.db.auctionclient.view;

/**
 * Click event listener.
 * @param <ItemType> type of the item.
 */
public interface ListItemListener<ItemType> {
    /**
     * Callback method, which is triggered, when a list item is clicked.
     * @param item list item, which was clicked.
     */
    public void onClick(ItemType item);
}

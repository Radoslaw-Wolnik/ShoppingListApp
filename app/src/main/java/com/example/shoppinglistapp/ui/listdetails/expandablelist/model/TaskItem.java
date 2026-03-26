package com.example.shoppinglistapp.ui.listdetails.expandablelist.model;

public class TaskItem extends ListItem {
    private final long taskId;
    private final String description;
    private final boolean isDone;


    public TaskItem(long taskId, String description, boolean isDone) {
        this.taskId = taskId;
        this.description = description;
        this.isDone = isDone;
    }

    @Override
    public long getId() {
        return taskId;
    }

    // Getters
    public long getTaskId() { return taskId; }
    public String getDescription() { return description; }
    public boolean isDone() { return isDone; }
}
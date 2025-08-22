package cn.popcraft.villagepro.model;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class PlayerTaskData {
    private UUID playerUuid;
    private Task currentTask;
    private Map<UUID, Task> activeTasks = new HashMap<>();

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task task) {
        this.currentTask = task;
        if (task != null) {
            activeTasks.put(task.getTaskId(), task);
        }
    }

    public List<Task> getActiveTasks() {
        return new ArrayList<>(activeTasks.values());
    }

    public Task getTaskById(UUID taskId) {
        return activeTasks.get(taskId);
    }

    public void removeTask(UUID taskId) {
        activeTasks.remove(taskId);
        if (currentTask != null && currentTask.getTaskId().equals(taskId)) {
            currentTask = null;
        }
    }
}
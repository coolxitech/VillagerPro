package cn.popcraft.villagepro.storage;

import cn.popcraft.villagepro.VillagePro;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.gson.Gson;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class SQLiteStorage {
    private final JavaPlugin plugin;
    private final Gson gson;
    private Connection connection;
    private final List<Class<?>> registeredTables = new ArrayList<>();

    public SQLiteStorage(JavaPlugin plugin, Gson gson) {
        this.plugin = plugin;
        this.gson = gson;
        initialize();
    }

    public void updateTaskProgress(UUID playerId, UUID taskId, int progress) {
        String sql = "UPDATE player_tasks SET progress = ? WHERE player_uuid = ? AND task_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, progress);
            stmt.setString(2, playerId.toString());
            stmt.setString(3, taskId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void completeTask(UUID playerId, UUID taskId) {
        String sql = "UPDATE player_tasks SET completed = 1 WHERE player_uuid = ? AND task_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, taskId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public <T> void registerTable(Class<T> clazz) {
        if (!registeredTables.contains(clazz)) {
            registeredTables.add(clazz);
            try {
                createTable(clazz);
            } catch (SQLException e) {
                plugin.getLogger().severe("注册表失败: " + e.getMessage());
            }
        }
    }

    private void initialize() {
        try {
            // 确保数据目录存在
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/data.db");
            
            // 为不同的数据模型创建表
            createPlayerTasksTable();
            createVillagesTable();
            createCropStorageTable();
        } catch (Exception e) {
            plugin.getLogger().severe("数据库初始化失败: " + e.getMessage());
        }
    }

    private void createPlayerTasksTable() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS player_tasks (" +
                "player_uuid TEXT NOT NULL, " +
                "task_id TEXT PRIMARY KEY, " +
                "type TEXT NOT NULL, " +
                "target_item TEXT, " +
                "target_amount INTEGER, " +
                "progress INTEGER DEFAULT 0, " +
                "reward_exp INTEGER, " +
                "reward_money REAL, " +
                "description TEXT, " +
                "completed INTEGER DEFAULT 0)"
            );
        }
    }
    
    private void createVillagesTable() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS villages (" +
                "owner_uuid TEXT PRIMARY KEY, " +
                "data TEXT NOT NULL)"
            );
        }
    }
    
    private void createCropStorageTable() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS crop_storage (" +
                "player_uuid TEXT PRIMARY KEY, " +
                "data TEXT NOT NULL)"
            );
        }
    }

    private void createTable(Class<?> clazz) throws SQLException {
        // 这里可以根据类名创建不同的表结构
        String tableName = clazz.getSimpleName().toLowerCase();
        try (Statement statement = connection.createStatement()) {
            // 创建通用的键值存储表
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id TEXT PRIMARY KEY, " +
                "data TEXT NOT NULL)"
            );
        }
    }

    public <T> List<T> findAll(Class<T> clazz) {
        List<T> results = new ArrayList<>();
        String tableName = clazz.getSimpleName().toLowerCase();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName)) {
            while (rs.next()) {
                // 这里应该实现实际的反序列化逻辑
                // 简化处理，返回空列表
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("查询失败: " + e.getMessage());
        }
        return results;
    }

    public <T> void save(T data) {
        // 实现保存逻辑
        // 简化版本，实际应该使用序列化
        if (data == null) return;
        
        // 根据数据类型保存到对应的表
        if (data.getClass().getSimpleName().equals("PlayerTaskData")) {
            savePlayerTaskData((cn.popcraft.villagepro.model.PlayerTaskData) data);
        } else if (data.getClass().getSimpleName().equals("Village")) {
            saveVillage((cn.popcraft.villagepro.model.Village) data);
        } else if (data.getClass().getSimpleName().equals("CropStorage")) {
            saveCropStorage((cn.popcraft.villagepro.model.CropStorage) data);
        }
    }
    
    private void savePlayerTaskData(cn.popcraft.villagepro.model.PlayerTaskData data) {
        // 实现PlayerTaskData的保存逻辑
        // 简化处理
    }
    
    private void saveVillage(cn.popcraft.villagepro.model.Village data) {
        // 实现Village的保存逻辑
        // 简化处理
    }
    
    private void saveCropStorage(cn.popcraft.villagepro.model.CropStorage data) {
        // 实现CropStorage的保存逻辑
        // 简化处理
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("数据库关闭失败: " + e.getMessage());
        }
    }
}
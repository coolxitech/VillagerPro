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
                createTable(clazz.getSimpleName());
            } catch (SQLException e) {
                plugin.getLogger().severe("注册表失败: " + e.getMessage());
            }
        }
    }

    private void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/data.db");
            createTable("default");
        } catch (Exception e) {
            plugin.getLogger().severe("数据库初始化失败: " + e.getMessage());
        }
    }

    private void createTable(String tableName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT NOT NULL)");
        }
    }

    public <T> List<T> findAll(Class<T> clazz) {
        List<T> results = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM " + clazz.getSimpleName())) {
            while (rs.next()) {
                // 这里简化处理，实际应该使用反序列化
                // 由于我们没有完整实现，这里返回空列表
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("查询失败: " + e.getMessage());
        }
        return results;
    }

    public <T> void save(T data) {
        // 实现保存逻辑
        // 简化版本，实际应该使用序列化
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
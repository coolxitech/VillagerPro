package cn.popcraft.villagepro.util;

import org.bukkit.plugin.java.JavaPlugin;
import com.google.gson.Gson;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SQLiteStorage<T> {
    private final JavaPlugin plugin;
    private final String tableName;
    private final Function<ResultSet, T> mapper;
    private final Gson gson;
    private Connection connection;

    public SQLiteStorage(JavaPlugin plugin, String tableName, Function<ResultSet, T> mapper) {
        this.plugin = plugin;
        this.tableName = tableName;
        this.mapper = mapper;
        this.gson = new Gson();
        
        // 初始化数据库连接
        initDatabase();
    }

    private void initDatabase() {
        try {
            // 确保数据目录存在
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            // 创建数据库连接
            String url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/data.db";
            connection = DriverManager.getConnection(url);
            
            // 创建表
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName + " (id TEXT PRIMARY KEY, data TEXT)");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("数据库初始化失败: " + e.getMessage());
        }
    }

    public void save(String id, T data) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO " + tableName + " (id, data) VALUES (?, ?)")) {
            stmt.setString(1, id);
            stmt.setString(2, gson.toJson(data));
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("保存数据失败: " + e.getMessage());
        }
    }

    public T find(String id) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT data FROM " + tableName + " WHERE id = ?")) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapper.apply(rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("查询数据失败: " + e.getMessage());
        }
        return null;
    }

    public List<T> findAll() {
        List<T> results = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT data FROM " + tableName);
            while (rs.next()) {
                results.add(mapper.apply(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("查询所有数据失败: " + e.getMessage());
        }
        return results;
    }

    public void delete(String id) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM " + tableName + " WHERE id = ?")) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("删除数据失败: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("关闭数据库连接失败: " + e.getMessage());
        }
    }
}
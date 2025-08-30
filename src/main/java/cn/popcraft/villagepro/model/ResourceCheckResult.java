package cn.popcraft.villagepro.model;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;

public class ResourceCheckResult {
    public boolean success;
    public double money;
    public Map<Material, Integer> items = new HashMap<>();
    public String message;
}
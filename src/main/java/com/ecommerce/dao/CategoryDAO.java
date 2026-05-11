package com.ecommerce.dao;

import com.ecommerce.config.DatabaseConfig;
import com.ecommerce.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);
    
    public boolean createCategory(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating category", e);
        }
        return false;
    }
    
    public Category getCategoryById(Integer id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
        } catch (SQLException e) {
            logger.error("Error getting category", e);
        }
        return null;
    }
    
    public List<Category> getAllCategories() {
        String sql = "SELECT * FROM categories";
        List<Category> categories = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting all categories", e);
        }
        return categories;
    }
    
    public boolean updateCategory(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setInt(3, category.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating category", e);
        }
        return false;
    }
    
    public boolean deleteCategory(Integer id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting category", e);
        }
        return false;
    }
    
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setCreatedAt(rs.getTimestamp("createdAt").getTime());
        return category;
    }
}
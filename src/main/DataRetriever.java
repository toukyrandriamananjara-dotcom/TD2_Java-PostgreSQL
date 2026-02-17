package main;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataRetriever {
    DBConnection dbConnection = new DBConnection();

    public Dish findDishById(int id) throws SQLException {
        Dish dish = new Dish();
        List<Ingredient> ingredients = new ArrayList<>();
        Connection connection = dbConnection.getConnection();
        try {

            StringBuilder list_plat = new StringBuilder("SELECT d.id as dish_id, d.name as dish_name," +
                    "d.dish_type as dish_type, i.id as ingredient_id, i.name as ingredient_name, i.price as ingredient_price, i.category as ingredient_category" +
                    " FROM Dish d join Ingredient i ON " +
                    "d.id=i.id_dish WHERE d.id = ?");

            PreparedStatement preparedStatement = connection.prepareStatement(list_plat.toString());
            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("ingredient_id"));
                ingredient.setName(resultSet.getString("ingredient_name"));
                ingredient.setPrice(resultSet.getDouble("ingredient_price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("ingredient_category")));


                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));

                ingredient.setDish(dish);
                ingredients.add(ingredient);
                dish.setIngredients(ingredients);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dish;

    }

    public List<Ingredient> findIngredients(int page, int size) {
        List<Ingredient> lists = new ArrayList<>();
        int offset = (page - 1) * size;
        Connection connection = dbConnection.getConnection();

        StringBuilder ingredient_list = new StringBuilder("SELECT * " +
                "FROM Ingredient ORDER BY id LIMIT ? OFFSET ?");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(ingredient_list.toString());
            preparedStatement.setInt(1, size);
            preparedStatement.setInt(2, offset);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));

                lists.add(ingredient);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lists;
    }

    List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        String insertSql = "INSERT INTO Ingredient(name,price,category) VALUES (?,?,?);";
        try {
            Connection connection = dbConnection.getConnection();
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql,
                    Statement.RETURN_GENERATED_KEYS)) {
                for (Ingredient ingredient : newIngredients) {
                    preparedStatement.setString(1, ingredient.getName());
                    preparedStatement.setDouble(2, ingredient.getPrice());
                    preparedStatement.setString(3, ingredient.getCategory() == null ? null : ingredient.getCategory().name());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    int i = 0;
                    while (generatedKeys.next()) {
                        newIngredients.get(i++).setId(generatedKeys.getInt(1));
                    }
                }
                connection.commit();
                return newIngredients;
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    ;

    List<Dish> findDishesByIngredientName(String ingredientName) {
        List<Dish> dishes = new ArrayList<>();
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT DISTINCT d.id AS dish_id, d.name AS dish_name, d.dish_type as " +
                    "dish_type, i.name as ingredient_name, i.price as ingredient_price " +
                    "FROM Dish d JOIN Ingredient i ON d.id = i.id_dish " +
                    "WHERE i.name ILIKE ? ORDER BY d.name ASC;");
            preparedStatement.setString(1, "%" + ingredientName + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));

                Ingredient ingredient = new Ingredient();
                ingredient.setName(resultSet.getString("ingredient_name"));
                ingredient.setPrice(resultSet.getDouble("ingredient_price"));

                dish.setIngredients(List.of(ingredient));
                dishes.add(dish);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dishes;
    }

    ;

    List<Ingredient> findIngredientsByCriteria(String ingredientName,
                                               CategoryEnum category,
                                               String dishName,
                                               int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT i.id AS ingredient_id, i.name AS ingredient_name, " +
                            "i.price, i.category, d.id AS dish_id, " +
                            "d.name AS dish_name FROM Ingredient i JOIN Dish d " +
                            "ON d.id = i.dish_id WHERE " +
                            "(? IS NULL OR i.name ILIKE ?) " +
                            "AND (? IS NULL OR i.category = ?) " +
                            "AND (? IS NULL OR d.name ILIKE ?) " +
                            "ORDER BY i.name ASC " +
                            "LIMIT ? OFFSET ?"
            );
            int index = 1;
            if (ingredientName != null) {
                preparedStatement.setString(index++, String.valueOf(Types.VARCHAR));
                preparedStatement.setString(index++, String.valueOf(Types.VARCHAR));
            } else {
                preparedStatement.setString(index++, ingredientName);
                preparedStatement.setString(index++, "%" + ingredientName + "%");
            }
            ;
            if (category != null) {
                preparedStatement.setString(index++, String.valueOf(Types.VARCHAR));
                preparedStatement.setString(index++, String.valueOf(Types.VARCHAR));
            } else {
                preparedStatement.setString(index++, category.name());
                preparedStatement.setString(index++, category.name());
            }
            ;
            if (dishName != null) {
                preparedStatement.setString(index++, String.valueOf(Types.VARCHAR));
                preparedStatement.setString(index++, String.valueOf(Types.VARCHAR));
            } else {
                preparedStatement.setString(index++, dishName);
                preparedStatement.setString(index++, "%" + dishName + "%");
            }
            ;
            preparedStatement.setInt(index++, size);
            preparedStatement.setInt(index, (page - 1) * size);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(resultSet.getInt("ingredient_id"));
                    ingredient.setName(resultSet.getString("ingredient_name"));
                    ingredient.setPrice(resultSet.getDouble("price"));
                    ingredient.setCategory(CategoryEnum.valueOf(category.name()));
                    Dish dish = new Dish();
                    dish.setId(resultSet.getInt("dish_id"));
                    dish.setName(resultSet.getString("dish_name"));
                    ingredients.add(ingredient);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ingredients;
    }

    ;

    public Dish saveDish(Dish dishToSave) {
        String upsertDishSql = """
            INSERT INTO dish (id, name, dish_type)
            VALUES (?, ?, ?::dish_type)
            ON CONFLICT (id) DO UPDATE
            SET name = EXCLUDED.name,
                dish_type = EXCLUDED.dish_type
            RETURNING id""";

        String deleteIngredientsSql = "DELETE FROM ingredient WHERE id_dish = ?";
        String insertIngredientSql = "INSERT INTO ingredient (name, price, category, id_dish) VALUES (?, ?, ?::category_enum, ?)";

        Connection conn = dbConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            Integer dishId;

            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                if (dishToSave.getId() != 0) {
                    ps.setInt(1, dishToSave.getId());
                } else {
                    ps.setInt(1, getNextDishId(conn));
                }

                ps.setString(2, dishToSave.getName());
                ps.setString(3, dishToSave.getDishType().name());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteIngredientsSql)) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }

            List<Ingredient> ingredients = dishToSave.getIngredients();
            if (ingredients != null && !ingredients.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(insertIngredientSql)) {
                    for (Ingredient ingredient : ingredients) {
                        ps.setString(1, ingredient.getName());
                        ps.setDouble(2, ingredient.getPrice());
                        ps.setString(3, ingredient.getCategory().name());
                        ps.setInt(4, dishId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
            return findDishById(dishId);

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                e.addSuppressed(ex);
            }
            throw new RuntimeException("Erreur lors de la sauvegarde du plat", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    private int getNextDishId(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM dish";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }





}

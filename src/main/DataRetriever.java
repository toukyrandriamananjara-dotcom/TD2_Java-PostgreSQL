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

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);

            Set<String> ingredientNames = new HashSet<>();
            for (Ingredient ingredient : newIngredients) {
                if (!ingredientNames.add(ingredient.getName())) {
                    throw new RuntimeException("Doublon détecté: " + ingredient.getName());
                }
            }

            String sql = "INSERT INTO Ingredient (name, price, category, id_dish) VALUES (?, ?, ?::category_enum, ?)";
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            for (Ingredient ingredient : newIngredients) {
                preparedStatement.setString(1, ingredient.getName());
                preparedStatement.setDouble(2, ingredient.getPrice());
                preparedStatement.setString(3, ingredient.getCategory().name());
                preparedStatement.setObject(4, ingredient.getDish() != null ? ingredient.getDish().getId() : null, Types.INTEGER);

                preparedStatement.executeUpdate();

                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    ingredient.setId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
            }

            preparedStatement.close();
            connection.commit();
            connection.setAutoCommit(true);
            connection.close();

            return newIngredients;

        } catch (SQLException e) {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Erreur rollback", rollbackEx);
            }

            if (e.getMessage().contains("unique constraint") || e.getMessage().contains("duplicate key")) {
                throw new RuntimeException("Ingrédient existe déjà: " + e.getMessage());
            }
            throw new RuntimeException("Erreur création ingrédients", e);
        }
    }


    public Dish saveDish(Dish dishToSave) {
        Connection connection = null;
        try {
            connection = dbConnection.getConnection();
            connection.setAutoCommit(false);

            if (dishExists(connection, dishToSave.getId())) {
                updateDish(connection, dishToSave);
            } else {
                insertDish(connection, dishToSave);
            }

            updateDishIngredients(connection, dishToSave);
            connection.commit();

            connection.setAutoCommit(true);
            dbConnection.closeConnection(connection);

            return findDishById(dishToSave.getId());

        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    dbConnection.closeConnection(connection);
                }
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Erreur lors du rollback", rollbackEx);
            }
            throw new RuntimeException("Erreur lors de la sauvegarde du plat", e);
        }
    }

    private boolean dishExists(Connection connection, int dishId) throws SQLException {
        if (dishId <= 0) return false;
        String sql = "SELECT COUNT(*) FROM Dish WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, dishId);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt(1);

        resultSet.close();
        preparedStatement.close();

        return count > 0;
    }

    private void insertDish(Connection connection, Dish dish) throws SQLException {
        String sql = "INSERT INTO Dish (name, dish_type) VALUES (?, ?::dish_type_enum)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, dish.getName());
        preparedStatement.setString(2, dish.getDishType().name());
        preparedStatement.executeUpdate();

        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            dish.setId(generatedKeys.getInt(1));
        }

        generatedKeys.close();
        preparedStatement.close();
    }

    private void updateDish(Connection connection, Dish dish) throws SQLException {
        String sql = "UPDATE Dish SET name = ?, dish_type = ?::dish_type_enum WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, dish.getName());
        preparedStatement.setString(2, dish.getDishType().name());
        preparedStatement.setInt(3, dish.getId());
        preparedStatement.executeUpdate();

        preparedStatement.close();
    }

    private void updateDishIngredients(Connection connection, Dish dish) throws SQLException {
        String dissociateSql = "UPDATE Ingredient SET id_dish = NULL WHERE id_dish = ?";
        PreparedStatement dissociateStmt = connection.prepareStatement(dissociateSql);
        dissociateStmt.setInt(1, dish.getId());
        dissociateStmt.executeUpdate();
        dissociateStmt.close();

        if (dish.getIngredients() != null && !dish.getIngredients().isEmpty()) {
            String associateSql = "UPDATE Ingredient SET id_dish = ? WHERE id = ?";
            PreparedStatement associateStmt = connection.prepareStatement(associateSql);

            for (Ingredient ingredient : dish.getIngredients()) {
                associateStmt.setInt(1, dish.getId());
                associateStmt.setInt(2, ingredient.getId());
                associateStmt.executeUpdate();
                ingredient.setDish(dish);
            }

            associateStmt.close();
        }
    }

    List<Dish> findDishesByIngredientName(String ingredientName){
        List<Dish> dishes = new ArrayList<>();
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT DISTINCT d.id AS dish_id, d.name AS dish_name " +
                    "FROM Dish d JOIN Ingredient i ON d.id = i.id_dish " +
                    "WHERE i.name ILIKE ? ORDER BY d.name ASC;");
            preparedStatement.setString(1, "%"+ingredientName+"%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dishes.add(dish);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dishes;
    };

    List<Ingredient> findIngredientsByCriteria(String ingredientName,
                                               CategoryEnum category,
                                               String dishName,
                                               int page, int size){
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
            if(ingredientName != null){
                preparedStatement.setString(index++, String.valueOf(Types.VARCHAR));
                preparedStatement.setString(index++, String.valueOf(Types.VARCHAR));
            } else{
                preparedStatement.setString(index++, ingredientName);
                preparedStatement.setString(index++, "%"+ingredientName+"%");
            };
            if(category != null){
                preparedStatement.setString(index++, String.valueOf(Types.VARCHAR));
                preparedStatement.setString(index++, String.valueOf(Types.VARCHAR));
            } else {
                preparedStatement.setString(index++, category.name());
                preparedStatement.setString(index++, category.name());
            };
            if(dishName != null){
                preparedStatement.setString(index++, String.valueOf(Types.VARCHAR));
                preparedStatement.setString(index++, String.valueOf(Types.VARCHAR));
            } else {
                preparedStatement.setString(index++, dishName);
                preparedStatement.setString(index++, "%"+dishName+"%");
            };
            preparedStatement.setInt(index++, size);
            preparedStatement.setInt(index, (page - 1)*size);

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
    };
}

package main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<Ingredient> findIngredients(int page, int size){
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

            while (resultSet.next()){
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

//    public List<Ingredient> createIngredient(List<Ingredient> newIngredient){
//        Connection connection = dbConnection.getConnection();
//
//        String createIngredient = "INSERT INTO Ingredient (id, name, price, category, id_dish) VALUES ?";
//    }
}

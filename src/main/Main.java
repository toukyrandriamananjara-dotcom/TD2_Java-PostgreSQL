package main;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();
//        try {
//            Dish dish = dataRetriever.findDishById(1);
//            System.out.println("ID: " + dish.getId());
//            System.out.println("Nom: " + dish.getName());
//            System.out.println("Type: " + dish.getDishType());
//            System.out.println("Nombre d'ingrédients: " + dish.getIngredients().size());
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }

        List<Ingredient> list = dataRetriever.findIngredients(1, 5);
        System.out.println(list);
    }
}

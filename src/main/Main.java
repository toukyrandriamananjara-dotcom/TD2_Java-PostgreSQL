package main;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        System.out.println(dataRetriever.findDishesByIngredientName("Tomate"));
    }
}

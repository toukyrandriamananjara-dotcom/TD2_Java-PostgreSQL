package main;

import java.util.List;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private List<Ingredient> ingredients;

    public Dish() {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = ingredients;
    }


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public DishTypeEnum getDishType() {
        return dishType;
    }
    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }
    public List<Ingredient> getIngredients() {
        return ingredients;
    }
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", ingredients=" + ingredients +
                '}';
    }

    public double getDishCoast() {
        if (ingredients == null || ingredients.isEmpty()) {
            return 0.0;
        }

        double total = 0;
        for (Ingredient ingredient : ingredients) {
            Double requiredQuantity = ingredient.getRequiredQuantity();

            if (requiredQuantity == null) {
                throw new IllegalStateException(
                        "Impossible de calculer le coût du plat '" + name + "' : " +
                                "la quantité nécessaire pour l'ingrédient '" + ingredient.getName() + "' est inconnue."
                );
            }

            total += ingredient.getPrice() * requiredQuantity;
        }
        return total;
    }
}


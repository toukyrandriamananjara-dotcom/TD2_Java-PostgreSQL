CREATE USER mini_dish_db_manager WITH PASSWORD '123456';

CREATE DATABASE first_mini_dish_db;

\c first_mini_dish_db;

GRANT ALL PRIVILEGES ON DATABASE first_mini_dish_db TO mini_dish_db_manager;
GRANT CREATE ON SCHEMA public TO mini_dish_db_manager;
GRANT USAGE ON SCHEMA public TO mini_dish_db_manager;
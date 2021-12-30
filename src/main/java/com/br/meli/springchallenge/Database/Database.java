package com.br.meli.springchallenge.Database;

import com.br.meli.springchallenge.Entity.Produto;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Database {

    private Connection connect() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:desafio1.db");
            return connection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<Produto> getAllProdutos() {
        return queryProduto("select * from produto order by name;");
    }

    public List<Produto> getAllProdutosByCategory(String category) {
        return queryProduto("select * from produto where category = '" + category + "'");
    }

    // Revisar
    public List<Produto> getAllProdutosByFilters(HashMap<String, String> filters) {

        String sqlQuery = "select * from produto where "; // category = '\" + category + \"'\"";

        for (Map.Entry<String, String> entry : filters.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();

            if (!key.equals("order")) {
                sqlQuery += key + " LIKE '" + value + "' and ";
            }
        }

        // retira o último and da query
        StringBuilder sb = new StringBuilder(sqlQuery);
//        for (int i = 0; i <= 4; i++) {
//            sb.deleteCharAt(sb.length() - 1);
//        }
        sb.delete(sb.length()-5, sb.length()-1);

        String orderBy = " ORDER BY 1";

        if (filters.containsKey("order")) {
            String orderStr = filters.get("order");
            int orderNum = Integer.parseInt(orderStr);

            switch (orderNum) {
                case 0:
                    orderBy = " ORDER BY name";
                    break;
                case 1:
                    orderBy = " ORDER BY name DESC";
                    break;
                case 2:
                    orderBy = " ORDER BY price DESC";
                    break;
                case 3:
                    orderBy = " ORDER BY price ASC";
                    break;
            }
        }
        sb.append(orderBy);

        return queryProduto(sb.toString());
    }

    public List<Produto> getAllProdutos(String category) {
        return queryProduto("select * from produto where category = " + category);
    }

    private List<Produto> queryProduto(String query) {
        try {
            Connection cn = connect();

            System.out.println("Conexão realizada !!!!");
            Statement statement = cn.createStatement();

            // lendo os registros
            PreparedStatement stmt = cn.prepareStatement(query);

            // tentar implementar a parametrização da query
//            PreparedStatement stmt = cn.prepareStatement("Select * from produtos where produtId = ?");
//            stmt.setInt(1, 4);

            ResultSet resultSet = stmt.executeQuery();

            List<Produto> produtos = new ArrayList<Produto>();

            while (resultSet.next()) {
                // Tentando fazer o cast da linha para o objeto Produto
//                Produto produto = (Produto) resultSet.getRow();

                Produto produto = new Produto(
                    resultSet.getLong("productId"),
                    resultSet.getString("name"),
                    resultSet.getString("category"),
                    resultSet.getString("brand"),
                    resultSet.getBigDecimal("price"),
                    resultSet.getInt("quantity"),
                    resultSet.getBoolean("free_shipping"),
                    resultSet.getString("prestige")
                );
                produtos.add(produto);
            }

            return produtos;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void customQuery(String query) {
        try {
            Connection cn = connect();

            Statement statement = cn.createStatement();

            PreparedStatement stmt = cn.prepareStatement(query);

            stmt.execute(); //executeQuery();

            System.out.println("Query executada com sucesso em customQuery()");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Produto> insertProdutoList(List<Produto> produtos) {
            List<Produto> listaProdutosCadastrados = new ArrayList<>();
            for (Produto elem : produtos) {
                listaProdutosCadastrados.add(insertProdutoSingle(elem));
            }
            return listaProdutosCadastrados;
    }

    private Produto insertProdutoSingle(Produto produto) {
        try {
            Connection cn = connect();
            String subquery = "INSERT INTO produto (name, category, brand, price, quantity, free_shipping, prestige) VALUES (?, ?, ?, ?, ?, ?, ?) ";
            PreparedStatement stmt = cn.prepareStatement(subquery, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, produto.getName());
            stmt.setString(2, produto.getCategory());
            stmt.setString(3, produto.getBrand());
            stmt.setBigDecimal(4, produto.getPrice());
            stmt.setInt(5, produto.getQuantity());
            stmt.setBoolean(6, produto.getFreeShipping());
            stmt.setString(7, produto.getPrestige());

            int linhasCriadas = stmt.executeUpdate(); //executeQuery();
            ResultSet keysSet = stmt.getGeneratedKeys();
            Produto produtoCriado;
            while (keysSet.next()) {
                int key = keysSet.getInt(1);

                PreparedStatement getStmt = cn.prepareStatement("Select * from produto where productId = ?");
                getStmt.setInt(1, key);
                ResultSet resultSet = getStmt.executeQuery();

                while (resultSet.next()) {
                    // Tentando fazer o cast da linha para o objeto Produto
                    //                Produto produto = (Produto) resultSet.getRow();

                    produtoCriado = new Produto(
                            resultSet.getLong("productId"),
                            resultSet.getString("name"),
                            resultSet.getString("category"),
                            resultSet.getString("brand"),
                            resultSet.getBigDecimal("price"),
                            resultSet.getInt("quantity"),
                            resultSet.getBoolean("free_shipping"),
                            resultSet.getString("prestige")
                    );
                    cn.close();
                    return produtoCriado;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
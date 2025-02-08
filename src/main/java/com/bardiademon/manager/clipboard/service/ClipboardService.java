package com.bardiademon.manager.clipboard.service;

import com.bardiademon.Jjson.array.JjsonArray;
import com.bardiademon.manager.clipboard.controller.DataSourceProvider;
import com.bardiademon.manager.clipboard.data.entity.ClipboardEntity;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import com.bardiademon.manager.clipboard.data.mapper.ClipboardMapper;
import com.bardiademon.manager.clipboard.data.repository.ClipboardRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ClipboardService implements ClipboardRepository {

    private static ClipboardRepository clipboardRepository;

    private ClipboardService() {
    }

    public static ClipboardRepository repository() {
        if (clipboardRepository == null) {
            clipboardRepository = new ClipboardService();
        }
        return clipboardRepository;
    }

    @Override
    public void saveClipboard(String name, String data, ClipboardType clipboardType) {
        System.out.printf("Starting saveClipboard, Name: %s , Data: %s , Type: %s\n", name, data, clipboardType);

        String query = """
                insert into "clipboard" ("name", "data", "type") values (?, ?, ?)
                """;

        System.out.printf("Executing -> Query: %s , Params: %s , ParamsSize: %s\n", query, JjsonArray.ofArray(new Object[]{name, data, clipboardType.name()}), 3);
        try (Connection connection = DataSourceProvider.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, data);
            preparedStatement.setString(3, clipboardType.name());
            int updated = preparedStatement.executeUpdate();

            if (updated <= 0) {
                System.out.printf("Failed to save clipboard, Name: %s , Data: %s , Type: %s\n", name, data, clipboardType);
                throw new SQLException("Failed to save clipboard");
            }

            System.out.printf("Successfully save clipboard, Name: %s , Data: %s , Type: %s\n", name, data, clipboardType);

        } catch (SQLException e) {
            System.out.printf("Failed to save clipboard, Name: %s , Data: %s , Type: %s , Exception: %s\n", name, data, clipboardType, e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    @Override
    public List<ClipboardEntity> fetchClipboards(int start, int end) {
        System.out.printf("Starting saveClipboard, Start: %d , End: %d", start, end);

        String query = """
                select
                        "id",
                        "name",
                        "data",
                        "type",
                        strftime('%Y/%m/%d %H:%M:%S', "created_at") as "created_at"
                from "clipboard"
                    limit ? offset ?
                """;

        System.out.printf("Executing -> Query: %s , Params: %s , ParamsSize: %s\n", query, JjsonArray.ofArray(new Object[]{start, end}), 2);
        try (Connection connection = DataSourceProvider.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            preparedStatement.setInt(1, start);
            preparedStatement.setInt(2, end);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.isBeforeFirst()) {
                    System.out.printf("Not found clipboard, Start: %d , End: %d\n", start, end);
                    return List.of();
                }

                List<ClipboardEntity> clipboardEntities = ClipboardMapper.toClipboardEntities(resultSet);

                System.out.println("Successfully fetch clipboard, Clipboard: " + clipboardEntities);

                return clipboardEntities;
            }

        } catch (SQLException e) {
            System.out.printf("Failed to fetch clipboard, Start: %d , End: %d , Exception: %s\n", start, end, e.getMessage());
            e.printStackTrace(System.out);
        }

        return List.of();
    }

    @Override
    public List<ClipboardEntity> fetchClipboards() {
        System.out.println("Starting saveClipboard");

        String query = """
                select
                        "id",
                        "name",
                        "data",
                        "type",
                        strftime('%Y/%m/%d %H:%M:%S', "created_at") as "created_at"
                from "clipboard"
                    limit ? offset ?
                """;

        System.out.printf("Executing -> Query: %s , Params: %s , ParamsSize: %s\n", query, "null", 0);
        try (Connection connection = DataSourceProvider.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.isBeforeFirst()) {
                    System.out.println("Not found clipboard");
                    return List.of();
                }

                List<ClipboardEntity> clipboardEntities = ClipboardMapper.toClipboardEntities(resultSet);

                System.out.println("Successfully fetch clipboard, Clipboard: " + clipboardEntities);

                return clipboardEntities;
            }

        } catch (SQLException e) {
            System.out.printf("Failed to fetch clipboard, Exception: %s\n", e.getMessage());
            e.printStackTrace(System.out);
        }

        return List.of();
    }

    @Override
    public int fetchTotalClipboards() {
        System.out.println("Starting fetchTotalClipboards");

        String query = """
                select count("id") as count from "clipboard"
                """;

        System.out.printf("Executing -> Query: %s , Params: %s , ParamsSize: %s\n", query, "null", 0);
        try (Connection connection = DataSourceProvider.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                if (!resultSet.next()) {
                    System.out.println("Not found clipboard");
                    return 0;
                }

                return resultSet.getInt("count");
            }

        } catch (SQLException e) {
            System.out.printf("Failed to fetch clipboard, Exception: %s\n", e.getMessage());
            e.printStackTrace(System.out);
        }

        return 0;
    }

    @Override
    public void deleteClipboard(int limit) {
        System.out.println("Starting deleteClipboard, Limit: " + limit);

        String query = """
                delete from "clipboard" where "id" in (select "id" from "clipboard" order by "id" desc limit ?)
                """;

        System.out.printf("Executing -> Query: %s , Params: %s , ParamsSize: %s\n", query, "[" + limit + "]", 1);
        try (Connection connection = DataSourceProvider.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, limit);

            int updated = preparedStatement.executeUpdate();

            if (updated <= 0) {
                System.out.println("Failed to delete clipboard, Limit: " + limit);
                throw new SQLException("Failed to delete clipboard");
            }

            System.out.println("Successfully delete clipboard, Limit: " + limit);


        } catch (SQLException e) {
            System.out.println("Failed to delete clipboard, Exception: " + e.getMessage());
            e.printStackTrace(System.out);
        }

    }

}

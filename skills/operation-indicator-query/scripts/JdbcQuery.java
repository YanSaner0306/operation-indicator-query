import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Base64;
'''JDBC桥接'''
public final class JdbcQuery {
    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private static String encode(String value) {
        if (value == null) return "-";
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String[] args) throws Exception {
        String url = System.getenv("BRRP_SKILL_DB_URL");
        String user = System.getenv("BRRP_SKILL_DB_USER");
        String password = System.getenv("BRRP_SKILL_DB_PASSWORD");
        if (url == null || user == null || password == null) {
            throw new IllegalStateException("Database environment is incomplete");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String sql = decode(reader.readLine());
        String normalized = sql.stripLeading().toUpperCase();
        if (!normalized.startsWith("SELECT ") || sql.contains(";")) {
            throw new IllegalArgumentException("Only a single SELECT is allowed");
        }
        int parameterCount = Integer.parseInt(reader.readLine());
        String[] parameters = new String[parameterCount];
        for (int i = 0; i < parameterCount; i++) parameters[i] = decode(reader.readLine());

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.setReadOnly(true);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setQueryTimeout(Integer.parseInt(System.getenv().getOrDefault("BRRP_SKILL_QUERY_TIMEOUT", "10")));
                for (int i = 0; i < parameters.length; i++) statement.setString(i + 1, parameters[i]);
                try (ResultSet result = statement.executeQuery()) {
                    ResultSetMetaData metadata = result.getMetaData();
                    int columns = metadata.getColumnCount();
                    System.out.println(columns);
                    for (int i = 1; i <= columns; i++) {
                        if (i > 1) System.out.print("\t");
                        System.out.print(encode(metadata.getColumnLabel(i)));
                    }
                    System.out.println();
                    while (result.next()) {
                        for (int i = 1; i <= columns; i++) {
                            if (i > 1) System.out.print("\t");
                            System.out.print(encode(result.getString(i)));
                        }
                        System.out.println();
                    }
                }
            }
        }
    }
}

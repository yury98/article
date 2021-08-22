import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import wrapper.Data;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PostgreWindowBTree {
    private static List<Data> z = new ArrayList();
    private static List<List<Data>> y = new ArrayList<>();
    private static int j;
    private static int loopCount = 0;
    private static Connection conn;

    public static void main(String[] args) throws SQLException, IOException, ParseException {
        String url = "jdbc:postgresql://192.168.56.104:5432/data";
        Properties props = new Properties();
        props.setProperty("user","postgres");
        props.setProperty("password","ifirby");
        conn = DriverManager.getConnection(url, props);
        ObjectMapper mapper = new ObjectMapper();
        Data[] data = mapper.readValue(getData(), Data[].class);
        List<Data> dataArrayList = Arrays.asList(data);
        for (int i = 0; i < dataArrayList.size(); i++) {
            if (i % 100_000 == 0) {
                System.out.println(i);
                System.out.println(LocalDateTime.now());
            }
            processEvent(dataArrayList.get(i));
        }
        System.out.println("End");
        System.out.println(LocalDateTime.now());
        System.out.println(loopCount);
        conn.close();
    }

    public static void processEvent(Data e) throws SQLException, ParseException {
        createData(e);
        z.add(0, e);
        if (e.getQ() == 1) {
            List<Data> D = search(e.getDestinationBank(), e.getDestinationAccount(), e.getDestinationCheck(), false, e.getTimestamp());
            if (D.size() != 0) {
                for (Data d : D) {
                    if (d.getDestinationBank().equals(e.getSourceBank())
                            && d.getDestinationAccount().equals(e.getSourceAccount())
                            && d.getQ() == 0) {
                        // Петля обнаружена
//                    System.out.println("Петля обнаружена...");
                        loopCount++;
                        chains(1, e);
//                    System.out.println(y);
                        z.clear();
                        y.clear();
                        j = 0;
                        return;
                    }
                }
            }
        }
    }

    public static void chains(int i, Data data) throws SQLException, ParseException {
        List<Data> B = search(data.getSourceBank(), data.getSourceAccount(), null, true, data.getTimestamp());
        if (B.size() == 0) {
            y.add(j++, new ArrayList<>(z));
            z.set(i, null);
            return;
        }
        int i1 = i + 1;
        for (Data b : B) {
            if (b.getNumber() < z.get(i - 1).getNumber()) {
                continue;
            }
            if (z.size() <= i) {
                z.add(i, b);
            } else {
                z.set(i, b);
            }
            if ((b.getSourceBank().equals(data.getDestinationBank())
                    && b.getSourceAccount().equals(data.getDestinationAccount())
                    && b.getSourceCheck().equals(data.getDestinationCheck()))
                    && b.getDestinationBank().equals(z.get(0).getSourceBank())
                    && b.getDestinationAccount().equals(z.get(0).getSourceAccount())
                    && b.getDestinationCheck().equals(z.get(0).getSourceCheck())
                    && b.getQ() == 0
            ) {
                y.add(j++, new ArrayList<>(z));
                z.set(i, null);
            } else {
                chains(i1, new Data(null, b.getSourceBank(), b.getSourceAccount(), b.getSourceCheck(), data.getDestinationBank(), data.getDestinationAccount(), data.getDestinationCheck(), null, null));
            }
        }
        z.set(i, null);
    }

    private static List<Data> search(String bank, Integer destination, Integer check, Boolean isReverse, String date) throws SQLException, ParseException {
        PreparedStatement st = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        java.util.Date parsedDate = dateFormat.parse(date);
        if (check != null) {
            st = conn.prepareStatement("SELECT * FROM CHECKS WHERE sourceBank = ? and sourceAccount = ? and sourceCheck = ? and isReverse = ? AND ? - timestamp < INTERVAL '6 hours';");
            st.setString(1, bank);
            st.setInt(2, destination);
            st.setInt(3, check);
            st.setBoolean(4, isReverse);
            st.setTimestamp(5, new Timestamp(parsedDate.getTime()));
        } else {
            st = conn.prepareStatement("SELECT * FROM CHECKS WHERE sourceBank = ? and sourceAccount = ? and isReverse = ? AND ? - timestamp < INTERVAL '6 hours';");
            st.setString(1, bank);
            st.setInt(2, destination);
            st.setBoolean(3, isReverse);
            st.setTimestamp(4, new Timestamp(parsedDate.getTime()));
        }
        ResultSet rs = st.executeQuery();
        List<Data> result = new ArrayList<>();
        while (rs.next()) {
            result.add(new Data(
                    rs.getTimestamp(2).toString(),
                    rs.getString(3),
                    rs.getInt(4),
                    rs.getInt(5),
                    rs.getString(6),
                    rs.getInt(7),
                    rs.getInt(8),
                    rs.getInt(10),
                    rs.getInt(11)
            ));
        }
        rs.close();
        st.close();
        return result;
    }

    private static String getData() throws IOException {
        File file = new File("all_data.json");
        String fileStr = FileUtils.readFileToString(file, "UTF-8");
        return fileStr;
    }

    private static void createData(Data data) throws SQLException, ParseException {
        PreparedStatement st = conn.prepareStatement("INSERT INTO CHECKS(timestamp, sourceBank, sourceAccount, " +
                "sourceCheck, destinationBank, destinationAccount, destinationCheck, isReverse, q, number)\n" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, false, ?, ?)," +
                "(?, ?, ?, ?, ?, ?, ?, true, ?, ?)");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        java.util.Date parsedDate = dateFormat.parse(data.getTimestamp());
        st.setTimestamp(1, new Timestamp(parsedDate.getTime()));
        st.setString(2, data.getSourceBank());
        st.setInt(3, data.getSourceAccount());
        st.setInt(4, data.getSourceCheck());
        st.setString(5, data.getDestinationBank());
        st.setInt(6, data.getDestinationAccount());
        if (data.getDestinationCheck() == null) {
            st.setNull(7, Types.BIGINT);
        } else {
            st.setInt(7, data.getDestinationCheck());
        }
        st.setInt(8, data.getQ());
        st.setInt(9, data.getNumber());
        st.setTimestamp(10, new Timestamp(parsedDate.getTime()));
        st.setString(11, data.getDestinationBank());
        st.setInt(12, data.getDestinationAccount());
        if (data.getDestinationCheck() == null) {
            st.setNull(13, Types.BIGINT);
        } else {
            st.setInt(13, data.getDestinationCheck());
        }
        st.setString(14, data.getSourceBank());
        st.setInt(15, data.getSourceAccount());
        st.setInt(16, data.getSourceCheck());
        st.setInt(17, data.getQ());
        st.setInt(18, data.getNumber());
        st.execute();
        st.close();
    }
}

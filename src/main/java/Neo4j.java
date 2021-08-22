import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.InternalRelationship;
import scala.Int;
import wrapper.Data;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class Neo4j {
    public static Driver driver;
    // Номер события
    private static int i = 0;
    private static int loopCount = 0;

    public static void main(String[] args) throws IOException {
        driver = GraphDatabase.driver( "bolt://192.168.56.104:7687", AuthTokens.basic( "neo4j", "ifirby" ) );
        Session session = driver.session();
        // Удаление всех сущностей из базы
        session.run("MATCH (n)\n DETACH DELETE n");
        ObjectMapper mapper = new ObjectMapper();
        Data[] data = mapper.readValue(getData(), Data[].class);
        List<Data> dataArrayList = Arrays.asList(data);
        for (i = 0; i < 100_000; i++) {
            if (i % 10_000 == 0) {
                System.out.println(i);
                System.out.println(LocalDateTime.now());
            }
            processEvent(session, dataArrayList.get(i));
        }
        System.out.println("End");
        System.out.println(LocalDateTime.now());
        System.out.println(loopCount);
        session.close();
    }

    private static void processEvent(Session session, Data data) {
        String createdCheckId = createCheck(session, data);
        List<String> nexts = findNext(session, data);
        if (nexts != null && nexts.size() != 0) {
            for (String next : nexts) {
                session.run("MATCH (y:Check), (z:Check)\n" +
                                "WHERE y.id = $idy AND z.id = $idz\n" +
                                "CREATE (y)-[:Next{id1: y.id, id2: z.id}]->(z)",
                        parameters("idy", Integer.valueOf(next), "idz", Integer.valueOf(createdCheckId))
                );
            }
        }
        List<String> X = searchForLoop(session, data);
        if (X != null && X.size() != 0) {
            // Поиск цепочек
            //System.out.println("Петля найдена");
            loopCount++;
            for (String x : X) {
                searchPath(session, x, createdCheckId);
            }
        }
    }

    private static String getData() throws IOException {
        File file = new File("all_data.json");
        String fileStr = FileUtils.readFileToString(file, "UTF-8");
        return fileStr;
    }

    private static List<String> searchForLoop(Session session, Data data) {
        // Начнем новую транзакцию
        List<String> id = session.writeTransaction( new TransactionWork<List<String>>()
        {
            @Override
            public List<String> execute( Transaction tx )
            {
                if (data.getQ() != 1) {
                    return null;
                }
                Result result = tx.run( "MATCH (x:Check)\n" +
                                "WHERE x.sourceBank = $eDestinationBank AND x.sourceAccount = $eDestinationAccount AND x.sourceCheck = $eDestinationCheck\n" +
                                "AND x.destinationBank = $eSourceBank AND x.destinationAccount = $eSourceAccount AND x.destinationCheck = $eSourceCheck\n" +
                                "AND x.q = 0\n" +
                                "RETURN x.id",
                        parameters("eDestinationBank", data.getDestinationBank(),
                                "eDestinationAccount", data.getDestinationAccount(),
                                "eDestinationCheck", data.getDestinationCheck(),
                                "eSourceBank", data.getSourceBank(),
                                "eSourceAccount", data.getSourceAccount(),
                                "eSourceCheck", data.getSourceCheck()
                        ));
                List<Record> records = result.list();
                if (records.size() == 0) {
                    return null;
                } else {
                    return result.list().stream().map(x -> String.valueOf(x.get(0).asInt())).collect(Collectors.toList());
                }
            }
        } );
        return id;
    }

    private static String createCheck(Session session, Data data) {
        // Начнем новую транзакцию
        String id = session.writeTransaction( new TransactionWork<String>()
        {
            @Override
            public String execute( Transaction tx )
            {
                SimpleDateFormat format = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                SimpleDateFormat oldDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                String resDate = null;
                try {
                    Date parsedDate = oldDateFormat.parse(data.getTimestamp());
                    resDate = format.format(parsedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Result result = tx.run( "CREATE (z:Check{id: $i, " +
                                "timestamp:datetime($date), " +
                                "sourceBank: $eSourceBank, " +
                                "sourceAccount: $eSourceAccount, " +
                                "sourceCheck: $eSourceCheck, " +
                                "destinationBank: $eDestinationBank, " +
                                "destinationAccount: $eDestinationAccount, " +
                                "destinationCheck: $eDestinationCheck, \n" +
                                "q: $q})" +
                                "RETURN z.id",
                        parameters("i", i,
                                "date", resDate,
                                "eSourceBank", data.getSourceBank(),
                                "eSourceAccount", data.getSourceAccount(),
                                "eSourceCheck", data.getSourceCheck(),
                                "eDestinationBank", data.getDestinationBank(),
                                "eDestinationAccount", data.getDestinationAccount(),
                                "eDestinationCheck", data.getDestinationCheck(),
                                "q", data.getQ()
                        ));
                // Вернем id созданной ноды
                return String.valueOf(result.single().get(0).asInt());
            }
        } );
        return id;
    }

    private static List<String> findNext(Session session, Data data) {
        // Начнем новую транзакцию
        List<String> ids = session.writeTransaction( new TransactionWork<List<String>>()
        {
            @Override
            public List<String> execute(Transaction tx )
            {
                Result result = tx.run( "MATCH (y:Check)\n" +
                                "WHERE y.destinationBank = $eSourceBank AND y.destinationAccount = $eSourceAccount AND y.destinationCheck = $eSourceCheck\n" +
                                "RETURN y.id",
                        parameters("eSourceBank", data.getSourceBank(),
                                "eSourceAccount", data.getSourceAccount(),
                                "eSourceCheck", data.getSourceCheck()
                        ));
                return result.list().stream().map(x -> String.valueOf(x.get(0).asInt())).collect(Collectors.toList());
            }
        } );
        return ids;
    }

    private static String searchPath(Session session, String idx, String idz) {
        // Начнем новую транзакцию
        String id = session.writeTransaction( new TransactionWork<String>()
        {
            @Override
            public String execute( Transaction tx )
            {
                Result result = tx.run( "MATCH (x:Check), y=(x:Check)-[:Next*]->(z)\n" +
                                "WHERE x.id = $idx AND z.id = $idz\n" +
                                "RETURN relationships(y)",
                        parameters("idx", Integer.valueOf(idx), "idz", Integer.valueOf(idz)
                        ));
                List<Record> records = result.list();
//                for (Record record : records) {
//                    for (Value value : record.values()) {
//                        List<Object> relationships = value.asList();
//                        for (Object rel : relationships) {
//                            InternalRelationship curRel = (InternalRelationship) rel;
//                            System.out.print(String.format("%s -> %s;", curRel.get("id1"), curRel.get("id2")));
//                        }
//                    }
//                    System.out.println();
//                }
                return null;
            }
        } );
        return id;
    }

}


// Поиск петли (:2)
//    MATCH (x:Check)
//    WHERE x.sourceBank = '65' AND x.sourceAccount = 123 AND x.sourceCheck = 123
//    AND x.destinationBank = '65' AND x.destinationAccount = 125
//    RETURN id(x)
// Поиск цепочек (:4)
//    MATCH (x:Check), y=(a:Check)-[:Next*]->(x)
//    WHERE x.id = 0 AND a:BEGIN
//    RETURN relationships(y)
// Создание нового отношения (:6)
//    CREATE (z:Check{id: 2, timestamp:'10.10.2021 16:34:30', sourceBank: '64', sourceAccount: 123, sourceCheck: 123, destinationBank: '65', destinationAccount: 125, destinationCheck: 125})
//    RETURN z
// Поиск ... по дестинейшену (:7)
//    MATCH (y:Check)
//    WHERE y.destinationBank = '65' AND y.destinationAccount = 125
//    RETURN id(y)
// Установка началом (:9)
//    MATCH (z:Check) WHERE id(z) = 1 SET z:BEGIN
// Создание связи (:12)
//    MATCH (y:Check), (z:Check)
//    WHERE id(y) = 0 AND id(z) = 1
//    CREATE (y)-[:Next{id1: y.id, id2: z.id}]->(z)
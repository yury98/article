import com.fasterxml.jackson.databind.ObjectMapper;
import wrapper.Data;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CreateData {

    private static List<Data> result = new ArrayList<>();
    private static Random rand = new Random();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static void main(String[] args) throws IOException {
        LocalDateTime curDate = LocalDateTime.now();
        for (int i = 0; i < 10 * 60 * 60 * 24; i++) {
            LocalDateTime date = curDate.plusSeconds(i / 10);
            if (i % (10 * 60 * 30) == 0 && i != 0) {
                generateLoop(date, i);
                continue;
            }
            boolean isDestCheck = rand.nextBoolean();
            result.add(new Data(
                    date.format(formatter),
                    String.valueOf(rand.nextInt(100)),
                    rand.nextInt(1_000_000_000) + 1_000_000_000,
                    rand.nextInt(1_000_000_000) + 1_000_000_000,
                    String.valueOf(rand.nextInt(100)),
                    rand.nextInt(1_000_000_000) + 1_000_000_000,
                    isDestCheck ? rand.nextInt(1_000_000_000) + 1_000_000_000 : null,
                    isDestCheck ? 1 : 0,
                    i
                    )
            );
        }
        // Тест для петли
//        for (int i = 0; i < 4; i++) {
//            LocalDateTime date = curDate.plusSeconds(i);
//            if (i % 3 == 0 && i != 0) {
//                generateLoop(date);
//                continue;
//            }
//            result.add(new Data(
//                            date.format(formatter),
//                            String.valueOf(rand.nextInt(100)),
//                            rand.nextInt(1_000_000_000) + 1_000_000_000,
//                            rand.nextInt(1_000_000_000) + 1_000_000_000,
//                            String.valueOf(rand.nextInt(100)),
//                            rand.nextInt(1_000_000_000) + 1_000_000_000,
//                            rand.nextBoolean() ? rand.nextInt(1_000_000_000) + 1_000_000_000 : null
//                    )
//            );
//        }
        // Маппер для сериализации класса
        ObjectMapper mapper = new ObjectMapper();
        // Преобразование данных в строку
        String jsonResult = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(result);
        // Создаем новый файл в директории проекта
        FileWriter myWriter = new FileWriter("all_data.json");
        // Запишем в файл json
        myWriter.write(jsonResult);
        // Закроем файл
        myWriter.close();
    }

    private static void generateLoop(LocalDateTime date, int cur) {
        int i = rand.nextInt(10 * 60 * 30);
        Data loopData = result.get(cur - i);
        result.add(new Data(
                date.format(formatter),
                loopData.getDestinationBank(),
                loopData.getDestinationAccount(),
                rand.nextInt(1_000_000_000) + 1_000_000_000,
                loopData.getSourceBank(),
                loopData.getSourceAccount(),
                loopData.getSourceCheck(),
                1,
                cur
        ));
    }
}

package csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

public class CSVs {
    /**
     * @param isFirstLineHeader csv 파일의 첫 라인을 헤더(타이틀)로 처리할까요?
     */
    public static Table createTable(File csv, boolean isFirstLineHeader) throws FileNotFoundException {
        File file = new File("rsc/train.csv");

        Scanner scanner = new Scanner(file);

        ArrayList<String> headers = null;
        if (isFirstLineHeader) {
            String headerLine = scanner.nextLine();

            String[] splitHeader = headerLine.split(",");
            headers = new ArrayList<String>();

            for (String header : splitHeader) {
                if (header.equals("")) {
                    headers.add(null);
                }
                else {
                    headers.add(header);
                }
            }
        }

        ArrayList<ArrayList<String>> cols = new ArrayList<ArrayList<String>>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            ArrayList<String> split = new ArrayList<String>();

            int lineIndex = 0;
            while (lineIndex < line.length()) {
                String buffer = "";
                boolean quote = false;

                while (lineIndex < line.length()) {
                    if (line.charAt(lineIndex) == ',') {
                        if (quote) {
                            buffer += line.charAt(lineIndex);
                            ++lineIndex;
                        } else {
                            break;
                        }
                    } else if (line.charAt(lineIndex) == '\"') {
                        if (quote == false) {
                            quote = true;
                        } else if (lineIndex + 1 < line.length() && line.charAt(lineIndex + 1) == '\"') {
                            buffer += "\"";
                            ++lineIndex;
                        } else if (quote) {
                            quote = false;
                        }

                        ++lineIndex;
                    } else {
                        buffer += line.charAt(lineIndex);
                        ++lineIndex;
                    }
                }

                if (buffer.equals("")) {
                    split.add(null);
                } else {
                    split.add(buffer);
                }

                ++lineIndex;

                if (lineIndex == line.length() && line.charAt(lineIndex - 1) == ',') {
                    split.add(null);
                }
            }

            if (cols.size() == 0) {
                for (int i = 0; i < split.size(); ++i) {
                    cols.add(new ArrayList<String>());
                }
            }

            for (int i = 0; i < split.size(); ++i) {
                cols.get(i).add(split.get(i));
            }
        }

        return new TableImpl(cols, headers);
    }

    /**
     * @return 새로운 Table 객체를 반환한다. 즉, 첫 번째 매개변수 Table은 변경되지 않는다.
     */
    public static Table sort(Table table, int byIndexOfColumn, boolean isAscending, boolean isNullFirst) {
        Table newTable = table.selectRows(0, table.getRowCount());
        return newTable.sort(byIndexOfColumn, isAscending, isNullFirst);
    }

    /**
     * @return 새로운 Table 객체를 반환한다. 즉, 첫 번째 매개변수 Table은 변경되지 않는다.
     */
    public static Table shuffle(Table table) {
        Table newTable = table.selectRows(0, table.getRowCount());
        return newTable.shuffle();
    }
}

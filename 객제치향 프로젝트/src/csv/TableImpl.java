package csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;

class TableImpl implements Table {
    private ArrayList<ColumnImpl> columns;
    private int sz;

    TableImpl(ArrayList<ArrayList<String>> cols, ArrayList<String> headers) {
        this.columns = new ArrayList<ColumnImpl>();
        this.sz = cols.get(0).size();

        if (headers == null) {
            for (ArrayList<String> col : cols) {
                this.columns.add(new ColumnImpl(col, null));
            }
        }
        else {
            for (int i = 0; i < headers.size(); ++i) {
                this.columns.add(new ColumnImpl(cols.get(i), headers.get(i)));
            }
        }
    }

    public String toString() {
        String result = "<" + this.getClass().getName() + "@" + String.format("%x", this.hashCode()) + ">\n"; // todo -> classname이 tableimpl로 나오는데 이걸 table로 나오게 수정하기
        result += "RangeIndex: " + this.sz + " entries, 0 to " + (this.sz - 1) + "\n";
        result += "Data columns (total " + this.columns.size() + " columns):\n";

        int index_len_max = Integer.toString(this.columns.size()).length();

        int column_name_len_max = 6; // Column
        for (int i = 0; i < this.columns.size(); ++i) {
            String header = this.columns.get(i).getHeader();
            int name_len = 0;
            if (header == null) {
                name_len = 0;
            }
            else {
                name_len = header.length();
            }

            if (column_name_len_max < name_len) {
                column_name_len_max = name_len;
            }
        }

        long non_null_count_len_max = "Non-Null Count".length();
        for (Column col : this.columns) {
            long non_null_count_len = Long.toString(this.sz - col.getNullCount()).length() + " non-null".length();
            if (non_null_count_len > non_null_count_len_max) {
                non_null_count_len_max = non_null_count_len;
            }
        }

        for (int i = "#".length(); i < index_len_max; ++i) {
            result += " ";
        }
        result += "# |";

        for (int i = "Column".length(); i < column_name_len_max; ++i) {
            result += " ";
        }
        result += "Column |";

        for (int i = "Non-Null Count".length(); i < non_null_count_len_max; ++i) {
            result += " ";
        }
        result += "Non-Null Count |Dtype\n";

        for (int col_num = 0; col_num < this.columns.size(); ++col_num) {
            for (int i = Integer.toString(col_num).length(); i < index_len_max; ++i) {
                result += " ";
            }
            result += col_num + " |";

            String header = this.columns.get(col_num).getHeader();
            if (header == null) {
                for (int i = 0; i < column_name_len_max; ++i) {
                    result += " ";
                }
                result += " |";
            }
            else {
                for (int i = this.columns.get(col_num).getHeader().length(); i < column_name_len_max; ++i) {
                    result += " ";
                }
                result += this.columns.get(col_num).getHeader() + " |";
            }

            for (int i = Long.toString(this.sz - this.columns.get(col_num).getNullCount()).length() + " non-null".length(); i < non_null_count_len_max; ++i) {
                result += " ";
            }
            result += (this.sz - this.columns.get(col_num).getNullCount()) + " non-null |";

            if (this.columns.get(col_num).getType() == Integer.class) {
                result += "int\n";
            }
            else if (this.columns.get(col_num).getType() == Double.class) {
                result += "double\n";
            }
            else {
                result += "String\n";
            }
        }

        int double_count = 0;
        int int_count = 0;
        int string_count = 0;

        for (ColumnImpl col : this.columns) {
            Class type = col.getType();
            if (type == Integer.class) {
                ++int_count;
            }
            else if (type == Double.class) {
                ++double_count;
            }
            else {
                ++string_count;
            }
        }

        result += "dtypes: double(" + double_count + "), int(" + int_count + "), String(" + string_count + ")\n";
        return result;
    }

    public void print() {
        ArrayList<Integer> maxlen = new ArrayList<Integer>();
        for (int i = 0; i < this.columns.size(); ++i) {
            maxlen.add(0);
        }

        for (int i = 0; i < this.columns.size(); ++i) {
            String header = this.columns.get(i).getHeader();
            if (header != null) {
                maxlen.set(i, header.length());
            }
        }

        for (int i = 0; i < this.columns.size(); ++i) {
            int column_max_len = this.columns.get(i).maxLen();
            if (maxlen.get(i) < column_max_len) {
                maxlen.set(i, column_max_len);
            }
        }

        String headline = "";
        boolean head_alive = false;
        for (int i = 0; i < this.columns.size(); ++i) {
            String header = this.columns.get(i).getHeader();
            if (header == null) {
                for (int k = 0; k < maxlen.get(i); ++k) {
                    headline += " ";
                }
                headline += " |";
            }
            else {
                head_alive = true;
                for (int k = header.length(); k < maxlen.get(i); ++k) {
                    headline += " ";
                }
                headline += header + " |";
            }
        }

        if (head_alive) {
            System.out.println(headline);
        }

        for (int i = 0; i < this.sz; ++i) {
            String line = "";
            for (int k = 0; k < this.columns.size(); ++k) {
                Object targetObject = this.columns.get(k).getValue(i);
                String target = "null";

                if (targetObject != null) {
                    target = targetObject.toString();

                    if (this.columns.get(k).getType() == Double.class) {
                        target = Double.toString(Math.round(Double.parseDouble((String)targetObject) * 1000000) / 1000000.0);
                    }
                }

                for (int j = target.length(); j < maxlen.get(k); ++j) {
                    line += " ";
                }

                line += target;
                line += " |";
            }

            System.out.println(line);
        }
    }

    public Table getStats() { // todo -> count 출력 시 소수점 안나오게 하기
        ArrayList<String> statsNameColumn = new ArrayList<String>();
        statsNameColumn.add("count");
        statsNameColumn.add("mean");
        statsNameColumn.add("std");
        statsNameColumn.add("min");
        statsNameColumn.add("25%");
        statsNameColumn.add("50%");
        statsNameColumn.add("75%");
        statsNameColumn.add("max");

        ArrayList<ArrayList<String>> numericItems = new ArrayList<ArrayList<String>>();
        ArrayList<String> headers = new ArrayList<String>();
        headers.add("");

        for (ColumnImpl col : this.columns) {
            ArrayList<String> numericItem = col.getNumericItems();
            if (numericItem.size() != 0) {
                headers.add(col.getHeader());
                numericItems.add(numericItem);
            }
        }

        ArrayList<ArrayList<String>> statsCols = new ArrayList<ArrayList<String>>();
        statsCols.add(statsNameColumn);

        for (int i = 1; i < headers.size(); ++i) {
            ColumnImpl tmpCol = new ColumnImpl(numericItems.get(i - 1), null);
            ArrayList<String> stats = new ArrayList<String>();

            stats.add(Double.toString(tmpCol.count()));
            stats.add(Double.toString(tmpCol.getMean()));
            stats.add(Double.toString(tmpCol.getStd()));
            stats.add(Double.toString(tmpCol.getNumericMin()));
            stats.add(Double.toString(tmpCol.getQ1()));
            stats.add(Double.toString(tmpCol.getMedian()));
            stats.add(Double.toString(tmpCol.getQ3()));
            stats.add(Double.toString(tmpCol.getNumericMax()));
            statsCols.add(stats);
        }

        return new TableImpl(statsCols, headers);
    }

    public Table head() {
        return this.head(5);
    }

    public Table head(int lineCount) {
        ArrayList<String> headers = new ArrayList<String>();
        for (int i = 0; i < this.columns.size(); ++i) {
            headers.add(this.columns.get(i).getHeader());
        }

        ArrayList<ArrayList<String>> headTable = new ArrayList<ArrayList<String>>();
        int availableLineCount = lineCount;
        if (availableLineCount > this.sz) {
            availableLineCount = this.sz;
        }

        for (int colNum = 0; colNum < this.columns.size(); ++colNum) {
            ArrayList<String> col = new ArrayList<String>();
            final ArrayList<Object> origin = this.columns.get(colNum).getItems();

            for (int line = 0; line < availableLineCount; ++line) {
                final Object obj = origin.get(line);
                if (obj == null) {
                    col.add(null);
                }
                else {
                    col.add(obj.toString());
                }
            }

            headTable.add(col);
        }

        return new TableImpl(headTable, headers);
    }

    public Table tail() {
        return this.tail(5);
    }

    public Table tail(int lineCount) {
        ArrayList<String> headers = new ArrayList<String>();
        for (int i = 0; i < this.columns.size(); ++i) {
            headers.add(this.columns.get(i).getHeader());
        }

        ArrayList<ArrayList<String>> tailTable = new ArrayList<ArrayList<String>>();
        int availableLineCount = lineCount;
        if (availableLineCount > this.sz) {
            availableLineCount = this.sz;
        }

        for (int colNum = 0; colNum < this.columns.size(); ++colNum) {
            ArrayList<String> col = new ArrayList<String>();
            final ArrayList<Object> origin = this.columns.get(colNum).getItems();

            for (int line = this.sz - availableLineCount; line < this.sz; ++line) {
                final Object obj = origin.get(line);
                if (obj == null) {
                    col.add(null);
                }
                else {
                    col.add(obj.toString());
                }
            }

            tailTable.add(col);
        }

        return new TableImpl(tailTable, headers);
    }

    public Table selectRows(int beginIndex, int endIndex) {
        ArrayList<String> headers = new ArrayList<String>();
        for (int i = 0; i < this.columns.size(); ++i) {
            headers.add(this.columns.get(i).getHeader());
        }

        ArrayList<ArrayList<String>> selectTable = new ArrayList<ArrayList<String>>();

        if (beginIndex >= endIndex || beginIndex > this.sz || endIndex < 0) {
            return new TableImpl(selectTable, headers);
        }

        int availableBegin = beginIndex;
        int availableEnd = endIndex;

        if (availableBegin < 0) {
            availableBegin = 0;
        }

        if (availableEnd > this.sz) {
            availableEnd = this.sz;
        }

        for (int colNum = 0; colNum < this.columns.size(); ++colNum) {
            ArrayList<String> col = new ArrayList<String>();
            final ArrayList<Object> origin = this.columns.get(colNum).getItems();

            for (int line = availableBegin; line < availableEnd; ++line) {
                final Object obj = origin.get(line);
                if (obj == null) {
                    col.add(null);
                }
                else {
                    col.add(obj.toString());
                }
            }

            selectTable.add(col);
        }

        return new TableImpl(selectTable, headers);
    }

    public Table selectRowsAt(int ...indices) {
        ArrayList<String> headers = new ArrayList<String>();
        for (int i = 0; i < this.columns.size(); ++i) {
            headers.add(this.columns.get(i).getHeader());
        }

        ArrayList<ArrayList<String>> selectTable = new ArrayList<ArrayList<String>>();

        for (int colNum = 0; colNum < this.columns.size(); ++colNum) {
            ArrayList<String> col = new ArrayList<String>();
            final ArrayList<Object> origin = this.columns.get(colNum).getItems();

            for (int index : indices) {
                final Object obj = origin.get(index);
                if (obj == null) {
                    col.add(null);
                }
                else {
                    col.add(obj.toString());
                }
            }

            selectTable.add(col);
        }

        return new TableImpl(selectTable, headers);
    }

    public Table selectColumns(int beginIndex, int endIndex) {
        ArrayList<String> headers = new ArrayList<String>();
        for (int i = beginIndex; i < endIndex; ++i) {
            headers.add(this.columns.get(i).getHeader());
        }

        ArrayList<ArrayList<String>> selectTable = new ArrayList<ArrayList<String>>();

        for (int colNum = beginIndex; colNum < endIndex; ++colNum) {
            ArrayList<String> col = new ArrayList<String>();
            final ArrayList<Object> origin = this.columns.get(colNum).getItems();

            for (int line = 0; line < origin.size(); ++line) {
                final Object obj = origin.get(line);
                if (obj == null) {
                    col.add(null);
                }
                else {
                    col.add(obj.toString());
                }
            }

            selectTable.add(col);
        }

        return new TableImpl(selectTable, headers);
    }

    public Table selectColumnsAt(int ...indices) {
        ArrayList<String> headers = new ArrayList<String>();
        for (int index : indices) {
            headers.add(this.columns.get(index).getHeader());
        }

        ArrayList<ArrayList<String>> selectTable = new ArrayList<ArrayList<String>>();

        for (int colNum : indices) {
            ArrayList<String> col = new ArrayList<String>();
            final ArrayList<Object> origin = this.columns.get(colNum).getItems();

            for (int line = 0; line < origin.size(); ++line) {
                final Object obj = origin.get(line);
                if (obj == null) {
                    col.add(null);
                }
                else {
                    col.add(obj.toString());
                }
            }

            selectTable.add(col);
        }

        return new TableImpl(selectTable, headers);
    }

    public <T> Table selectRowsBy(String columnName, Predicate<T> predicate) { // 마지막!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        ArrayList<Integer> indices = new ArrayList<Integer>();

        ColumnImpl targetColumn = null;
        for (ColumnImpl col : this.columns) {
            if (col.getHeader().equals(columnName)) {
                targetColumn = col;
                break;
            }
        }

        if (targetColumn == null) {
            return null;
        }

        ArrayList<Object> columnItems = targetColumn.getItems();
        for (int i = 0; i < columnItems.size(); ++i) {
            try {
                if (targetColumn.getType() == Double.class || targetColumn.getType() == String.class) {
                    if (predicate.test((T)columnItems.get(i))) {
                        indices.add(i);
                    }
                } else {
                    if (predicate.test((T)Integer.valueOf(columnItems.get(i).toString()))) {
                        indices.add(i);
                    }
                }
            } catch (NullPointerException e) {

            } catch (ClassCastException e) {

            }
        }

        ArrayList<String> headers = new ArrayList<String>();
        for (int i = 0; i < this.columns.size(); ++i) {
            headers.add(this.columns.get(i).getHeader());
        }

        ArrayList<ArrayList<String>> selectTable = new ArrayList<ArrayList<String>>();

        for (int colNum = 0; colNum < this.columns.size(); ++colNum) {
            ArrayList<String> col = new ArrayList<String>();
            final ArrayList<Object> origin = this.columns.get(colNum).getItems();

            for (int index : indices) {
                final Object obj = origin.get(index);
                if (obj == null) {
                    col.add(null);
                }
                else {
                    col.add(obj.toString());
                }
            }

            selectTable.add(col);
        }

        return new TableImpl(selectTable, headers);
    }

    public Table sort(int byIndexOfColumn, boolean isAscending, boolean isNullFirst) {
        ArrayList<Integer> key = this.columns.get(byIndexOfColumn).sort(isAscending, isNullFirst);

        for (int i = 0; i < this.columns.size(); ++i) {
            if (i != byIndexOfColumn) {
                this.columns.get(i).sort(key);
            }
        }

        return this;
    }

    public Table shuffle() {
        ArrayList<Integer> key = new ArrayList<Integer>();
        for (int i = 0; i < this.sz; ++i) {
            key.add(i);
        }

        Collections.shuffle(key);

        for (int i = 0; i < this.columns.size(); ++i) {
            this.columns.get(i).sort(key);
        }

        return this;
    }

    public int getRowCount() {
        return this.sz;
    }

    public int getColumnCount() {
        return this.columns.size();
    }

    public Column getColumn(int index) {
        return this.columns.get(index);
    }

    public Column getColumn(String name) {
        for (int i = 0; i < this.columns.size(); ++i) {
            if (this.columns.get(i).getHeader().equals(name)) {
                return this.columns.get(i);
            }
        }

        return null;
    }

    public boolean fillNullWithMean() {
        boolean result = false;

        for (int i = 0; i < this.columns.size(); ++i) {
            result |= this.columns.get(i).fillNullWithMean();
        }

        return result;
    }

    public boolean fillNullWithZero() {
        boolean result = false;

        for (int i = 0; i < this.columns.size(); ++i) {
            result |= this.columns.get(i).fillNullWithZero();
        }

        return result;
    }

    public boolean standardize() {
        boolean result = false;

        for (int i = 0; i < this.columns.size(); ++i) {
            result |= this.columns.get(i).standardize();
        }

        return result;
    }

    public boolean normalize() {
        boolean result = false;

        for (int i = 0; i < this.columns.size(); ++i) {
            result |= this.columns.get(i).normalize();
        }

        return result;
    }

    public boolean factorize() {
        boolean result = false;

        for (int i = 0; i < this.columns.size(); ++i) {
            result |= this.columns.get(i).factorize();
        }

        return result;
    }
}

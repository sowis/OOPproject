package csv;

import java.util.ArrayList;
import java.util.Collections;

class ColumnImpl implements Column {
    private String header;
    private Class type;
    private ArrayList<Object> arr;

    ColumnImpl(ArrayList<String> arr, String header) {
        this.header = header;
        this.arr = new ArrayList<Object>();

        boolean intClass = true;
        boolean doubleClass = true;

        for (String s : arr) {
            if (s == null) {
                continue;
            }

            try {
                Integer.parseInt(s);
            }
            catch (NumberFormatException e) {
                intClass = false;
            }

            try {
                Double.parseDouble(s);
            }
            catch (NumberFormatException e) {
                doubleClass = false;
            }
        }

        if (intClass == true) {
            this.type = Integer.class;
        }
        else if (doubleClass == true) {
            this.type = Double.class;
        }
        else {
            this.type = String.class;
        }

        for (String s : arr) {
            if (s == null) {
                this.arr.add(null);
                continue;
            }

            if (this.type == Integer.class) {
                this.arr.add(Integer.parseInt(s));
            }
            else if (this.type == Double.class) {
                this.arr.add(Double.parseDouble(s));
            }
            else {
                this.arr.add(s);
            }
        }
    }

    Class getType() {
        return this.type;
    }

    ArrayList<String> getNumericItems() {
        ArrayList<String> result = new ArrayList<String>();

        for (Object o : this.arr) {
            try {
                double d = Double.parseDouble(o.toString());
                result.add(Double.toString(d));
            }
            catch (Exception e) {

            }
        }

        return result;
    }

    ArrayList<Object> getItems() {
        return this.arr;
    }

    ArrayList<Integer> sort(boolean isAscending, boolean isNullFirst) {
        int objBegin = 0;
        int objEnd = this.arr.size() - 1;
        ArrayList<Integer> key = new ArrayList<Integer>(this.arr.size());
        for (int i = 0; i < this.arr.size(); ++i) {
            key.add(0);
        }

        for (int i = 0; i < key.size(); ++i) {
            key.set(i, i);
        }

        if (isNullFirst) {
            for (;objBegin < this.arr.size() && this.arr.get(objBegin) == null; ++objBegin);

            for (int i = objBegin; i <this.arr.size(); ++i) {
                if (this.arr.get(i) == null) {
                    Collections.swap(this.arr, objBegin, i);
                    Collections.swap(key, objBegin, i);
                    ++objBegin;
                }
            }
        }
        else {
            for (;objEnd >= 0 && this.arr.get(objEnd) == null; --objEnd);

            for (int i = objEnd; i >= 0; --i) {
                if (this.arr.get(i) == null) {
                    Collections.swap(this.arr, objEnd, i);

                    Collections.swap(key, objEnd, i);
                    --objEnd;
                }
            }
        }

        if (isAscending) {
            for (int i = 0; i < objEnd - objBegin; ++i) {
                for (int k = objBegin; k < objEnd - i; ++k) {
                    if (this.type == String.class) {
                        if (((String)this.arr.get(k)).compareTo((String)this.arr.get(k + 1)) > 0) {
                            Collections.swap(this.arr, k, k + 1);
                            Collections.swap(key, k, k + 1);
                        }
                    }
                    else {
                        if (Double.parseDouble(this.arr.get(k).toString()) > Double.parseDouble(this.arr.get(k + 1).toString())) {
                            Collections.swap(this.arr, k, k + 1);
                            Collections.swap(key, k, k + 1);
                        }
                    }
                }
            }
        }
        else {
            for (int i = 0; i < objEnd - objBegin; ++i) {
                for (int k = objBegin; k < objEnd - i; ++k) {
                    if (this.type == String.class) {
                        if (((String)this.arr.get(k)).compareTo((String)this.arr.get(k + 1)) < 0) {
                            Collections.swap(this.arr, k, k + 1);
                            Collections.swap(key, k, k + 1);
                        }
                    }
                    else {
                        if (Double.parseDouble(this.arr.get(k).toString()) < Double.parseDouble(this.arr.get(k + 1).toString())) {
                            Collections.swap(this.arr, k, k + 1);
                            Collections.swap(key, k, k + 1);
                        }
                    }
                }
            }
        }

        return key;
    }

    void sort(ArrayList<Integer> key) {
        ArrayList<Object> new_arr = new ArrayList<Object>();
        for (int i = 0; i < this.arr.size(); ++i) {
            new_arr.add(new Object());
        }

        for (int source = 0; source < key.size(); ++source) {
            int target = key.get(source);
            new_arr.set(target, this.arr.get(source));
        }

        this.arr = new_arr;
    }

    int maxLen() {
        int result = 0;

        for (Object o : this.arr) {
            if (o == null) {
                int len = "null".length();
                if (result < len) {
                    result = len;
                }
            }
            else {
                int len = o.toString().length();

                if (this.type == Double.class) {
                    len = String.format("%.6f", Double.valueOf(o.toString())).length();
                }

                if (result < len) {
                    result = len;
                }
            }
        }

        return result;
    }

    public String getHeader() {
        return this.header;
    }

    public String getValue(int index) {
        Object obj = this.arr.get(index);
        if (obj == null) {
            return null;
        }
        else {
            return obj.toString();
        }
    }

    public <T extends Number> T getValue(int index, Class<T> t) {
        return (T)this.arr.get(index);
    }

    public void setValue(int index, String value) {
        this.arr.set(index, value);
    }

    public <T extends Number> void setValue(int index, T value) {
        this.arr.set(index, value);
    }

    public int count() {
        return this.arr.size();
    }

    public void print() {
        if (this.header != null) {
            System.out.println(this.header);
        }

        for (Object o : this.arr) {
            if (o == null) {
                System.out.println("null");
                continue;
            }

            System.out.println(o.toString());
        }
    }

    public boolean isNumericColumn() {
        if (this.type == Number.class || this.type == Double.class || this.type == Integer.class) {
            return true;
        }

        return false;
    }

    public long getNullCount() {
        long count = 0;
        for (Object d : this.arr) {
            if (d == null) {
                ++count;
            }
        }

        return count;
    }

    public long getNumericCount() {
        long count = 0;
        for (Object o : this.arr) {
            if (o == null) {
                continue;
            }

            try {
                Double.parseDouble(o.toString());
                ++count;
            }
            catch (Exception e) {
            }
        }

        return count;
    }

    public double getNumericMin() {
        double result = Double.MAX_VALUE;
        for (Object o : this.arr) {
            if (o == null) {
                continue;
            }

            if (((Number)o).doubleValue() < result) {
                result = ((Number)o).doubleValue();
            }
        }

        return result;
    }

    public double getNumericMax() {
        double result = Double.MIN_VALUE;
        for (Object o : this.arr) {
            if (o == null) {
                continue;
            }

            if (((Number)o).doubleValue() > result) {
                result = ((Number)o).doubleValue();
            }
        }

        return result;
    }

    public double getMean() {
        double sum = 0;
        int count = 0;
        for (Object o : this.arr) {
            if (o == null) {
                continue;
            }

            sum += ((Number)o).doubleValue();
            ++count;
        }

        return sum / count;
    }

    public double getStd() {
        double mean = this.getMean();
        double div_sum = 0;
        int count = 0;

        for (Object o : this.arr) {
            if (o == null) {
                continue;
            }

            div_sum += Math.pow((((Number)o).doubleValue() - mean), 2);
            ++count;
        }

        double variance = div_sum / count;
        return Math.sqrt(variance);
    }

    public double getQ1() {
        ArrayList<Double> number_arr = new ArrayList<Double>();
        for (Object o : this.arr) {
            number_arr.add(((Number)o).doubleValue());
        }

        Collections.sort(number_arr);

        return number_arr.get(number_arr.size() / 4);
    }

    public double getMedian() {
        ArrayList<Double> number_arr = new ArrayList<Double>();
        for (Object o : this.arr) {
            number_arr.add(((Number)o).doubleValue());
        }

        Collections.sort(number_arr);

        return number_arr.get(number_arr.size() / 2);
    }

    public double getQ3() {
        ArrayList<Double> number_arr = new ArrayList<Double>();
        for (Object o : this.arr) {
            number_arr.add(((Number)o).doubleValue());
        }

        Collections.sort(number_arr);

        return number_arr.get(number_arr.size() * 3 / 4);
    }

    public boolean fillNullWithMean() {
        if (this.type == String.class) {
            return false;
        }

        boolean change = false;
        double mean = this.getMean();

        for (int i = 0; i < this.arr.size(); ++i) {
            if (arr.get(i) == null) {
                change = true;
                arr.set(i, mean);
            }
        }

        this.type = Double.class;
        return change;
    }

    public boolean fillNullWithZero() {
        if (this.type == String.class) {
            return false;
        }

        boolean change = false;

        for (int i = 0; i < this.arr.size(); ++i) {
            if (arr.get(i) == null) {
                change = true;
                arr.set(i, 0);
            }
        }

        return change;
    }

    public boolean standardize() {
        if (this.type == String.class) {
            return false;
        }

        double mean = this.getMean();
        double std = this.getStd();

        for (int i = 0; i < this.arr.size(); ++i) {
            if (this.arr.get(i) == null) {
                continue;
            }

            double value = ((Number)this.arr.get(i)).doubleValue();
            double standardization_value = (value - mean) / std;

            this.arr.set(i, standardization_value);
        }

        this.type = Double.class;
        return true;
    }

    public boolean normalize() {
        if (this.type == String.class) {
            return false;
        }

        double min = this.getNumericMin();
        double max = this.getNumericMax();

        for (int i = 0; i < this.arr.size(); ++i) {
            if (this.arr.get(i) == null) {
                continue;
            }

            double value = ((Number)this.arr.get(i)).doubleValue();
            double normalize_value = (value - min) / (max - min);

            this.arr.set(i, normalize_value);
        }

        this.type = Double.class;
        return true;
    }

    public boolean factorize() {
        ArrayList<Object> components = new ArrayList<Object>();

        for (Object o : this.arr) {
            if (o == null) {
                continue;
            }

            if (components.size() == 0) {
                components.add(o);
            }
            else if (components.size() == 1) {
                if (o.equals(components.get(0))) {
                    continue;
                }

                components.add(o);
            }
            else if (components.size() == 2) {
                if (o.equals(components.get(0))) {
                    continue;
                }

                if (o.equals(components.get(1))) {
                    continue;
                }

                return false;
            }
            else {
                return false;
            }
        }

        for (int i = 0; i < this.arr.size(); ++i) {
            if (this.arr.get(i) == null) {
                continue;
            }

            if (this.arr.get(i).equals(components.get(0))) {
                this.arr.set(i, 0);
            }
            else {
                this.arr.set(i, 1);
            }
        }

        this.type = Integer.class;
        return true;
    }
}
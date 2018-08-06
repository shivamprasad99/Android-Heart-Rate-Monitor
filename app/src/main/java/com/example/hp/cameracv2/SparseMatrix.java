package com.example.hp.cameracv2;

public class SparseMatrix {
    private double[][] data = new double[10][3];
    private int row, col; // size of the matrix
    private int len; // useful length of data
    public SparseMatrix(int r, int c) {
        row = r;
        col = c;
    }
    public void addData(double r, double c, double val) {
        if (len >= data.length) { // double capacity
            double[][] tmp = new double[data.length*2][3];
            for (int i = 0; i < len; i++) { // copy original data
                tmp[i][0] = data[i][0];
                tmp[i][1] = data[i][1];
                tmp[i][2] = data[i][2];
            }
            data = tmp; // data points to new array
        }
        data[len][0] = r;
        data[len][1] = c;
        data[len++][2] = val;
    }
    public SparseMatrix add(SparseMatrix b) {
        int aPos = 0, bPos = 0;
        SparseMatrix tmp = new SparseMatrix(row, col);
        while (aPos < len && bPos < b.len) { // when none is empty, scan both matrix
            if (data[aPos][0] > b.data[bPos][0] || (data[aPos][0] == b.data[bPos][0] && data[aPos][1] > b.data[bPos][1])) { // b smaller
                // copy b to new matrix
                tmp.addData(b.data[bPos][0], b.data[bPos][1], b.data[bPos][2]);
                bPos++;
            } else if (data[aPos][0] < b.data[bPos][0] || (data[aPos][0] == b.data[bPos][0] && data[aPos][1] < b.data[bPos][1])) { // a smaller
                // copy a to new matrix
                tmp.addData(data[aPos][0], data[aPos][1], data[aPos][2]);
                aPos++;
            } else { // data at aPos and bPos are the same row and column
                double rel = data[aPos][2] + b.data[bPos][2];
                if (rel != 0) tmp.addData(data[aPos][0], data[aPos][1], rel);
                aPos++;
                bPos++;
            }
        }
        // copy remain data
        while (aPos < len) tmp.addData(data[aPos][0], data[aPos][1], data[aPos++][2]);
        while (bPos < b.len) tmp.addData(b.data[bPos][0], b.data[bPos][1], b.data[bPos++][2]);
        return tmp;
    }
    public SparseMatrix transpose() {
        SparseMatrix tmp = new SparseMatrix(col, row);
        tmp.data = new double[len][3];
        tmp.len = len;
        tmp.row = col;
        tmp.col = row;
        int[] count = new int[col]; // count[i]: how many data in column i
        for (int i = 0; i < len; i++)
            count[(int)data[i][1]]++;
        int[] index = new int[col]; // index[i]: how many data have column smaller than i
        for (int i = 1; i < col; i++)
            index[i] = index[i-1] + count[i-1];
        for (int i = 0; i < len; i++) {
            int insertPos = index[(int)data[i][1]]++; // a new data inserted, so shift insertion point
            tmp.data[insertPos][0] = data[i][1]; // transpose
            tmp.data[insertPos][1] = data[i][0]; // transpose
            tmp.data[insertPos][2] = data[i][2]; // copy data
        }
        return tmp;
    }
    public SparseMatrix multiply(SparseMatrix x) {
        SparseMatrix b = x.transpose();
        int aPos, bPos;
        SparseMatrix rel = new SparseMatrix(row, b.row);
        for (aPos = 0; aPos < len; ) {
            double r = data[aPos][0]; // current row
            for (bPos = 0; bPos < b.len; ) {
                double c = b.data[bPos][0]; // current column
                int scanA = aPos;
                int scanB = bPos;
                double sum = 0;
                while (scanA < len && data[scanA][0] == r && scanB < b.len && b.data[scanB][0] == c) { // calculate rel[r][c]
                    if (data[scanA][1] < b.data[scanB][1]) // scanB has larger column
                        scanA++; // skip a
                    else if (data[scanA][1] > b.data[scanB][1]) // scanA has larger column
                        scanB++; // skip b
                    else // same column, so they can multiply
                        sum += data[scanA++][2] * b.data[scanB++][2];
                }
                if (sum != 0) rel.addData(r, c, sum);
                while (bPos < b.len && b.data[bPos][0] == c) bPos++; // jump to next column
            }
            while (aPos < len && data[aPos][0] == r) aPos++; // jump to next row
        }
        return rel;
    }
    public void print() {
        System.out.println("row = "+row+", column = "+col);
        for (int i = 0; i < len; i++) {
            System.out.print(data[i][0]);
            System.out.print(" ");
            System.out.print(data[i][1]);
            System.out.print(" ");
            System.out.print(data[i][2]);
            System.out.println();
        }
    }
}
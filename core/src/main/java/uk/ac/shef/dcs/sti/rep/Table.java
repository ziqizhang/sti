package uk.ac.shef.dcs.sti.rep;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix1D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;

import java.io.Serializable;
import java.util.*;


/**
 * An Table always has horizontally related columns. First row always headers
 *
 *
 *
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 01/10/12
 * Time: 15:06
 */
public class Table implements Serializable {
    private String sourceId;
    private String tableId;

    private String tableXPath;
    private Map<Integer, String> rowXPaths;

    private ObjectMatrix1D headers; //an object can only be a TColumnHeader object
    private ObjectMatrix2D contents;//an object can only be a TCell object


    private int rows; //# of rows in the table (excluding header)
    private int cols; //# of columns in the table

    //private List<CellBinaryRelationAnnotation> relations = new ArrayList<CellBinaryRelationAnnotation>();
    private List<TContext> contexts = new ArrayList<TContext>();

    private TAnnotation tableAnnotations;


    public Table(String id, String sourceId, int rows, int cols) {
        this.tableId = id;
        this.sourceId = sourceId;

        this.rows = rows;
        this.cols = cols;
        contents = new SparseObjectMatrix2D(rows, cols);
        headers = new SparseObjectMatrix1D(cols);

        rowXPaths = new LinkedHashMap<Integer, String>();
        tableAnnotations = new TAnnotation(rows, cols);
    }

    public int getNumRows() {
        return rows;
    }

    public void setNumRows(int row){
        this.rows=row;
    }

    public int getNumCols() {
        return cols;
    }
    public void setNumCols(int col){
        this.cols=col;
    }

      //single header/cell
    public void setColumnHeader(int c,TColumnHeader header){
        headers.set(c, header);
    }
    public TColumnHeader getColumnHeader(int c){
        Object o = headers.get(c);
        if(o==null)
            return null;

        return (TColumnHeader)o;
    }

    public void setContentCell(int r, int c, TCell cell) {
        contents.set(r, c, cell);
    }

    public TCell getContentCell(int r, int c) {
        return (TCell)contents.get(r, c);
    }


    //headers and content cells;
    protected ObjectMatrix1D getTableHeaders() {
       return headers;
    }
    protected ObjectMatrix2D getContentCells() {
        ObjectMatrix2D contentCells = contents.viewPart(1, 0, contents.rows() - 1, contents.columns());
        return contentCells;
    }


    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }


    public List<TContext> getContexts() {
        return contexts;
    }

    public void addContext(TContext context) {
        contexts.add(context);
    }

    public boolean equals(Object o) {
        if (o instanceof Table) {
            Table t = (Table) o;
            return t.getTableId().equals(getTableId()) && t.getSourceId().equals(getSourceId());
        }
        return false;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String toString() {
        return getSourceId() + "," + getTableId();
    }

    public String getTableXPath() {
        return tableXPath;
    }
    public void setTableXPath(String tableXPath) {
        this.tableXPath = tableXPath;
    }

    public Map<Integer, String> getRowXPaths() {
        return rowXPaths;
    }


    public TAnnotation getTableAnnotations() {
        return tableAnnotations;
    }

    public void setTableAnnotations(TAnnotation tableAnnotations) {
        this.tableAnnotations = tableAnnotations;
    }

    public int size(){
        return contents.size();
    }

    public int getNumHeaders() {
        return headers.size();
    }
}

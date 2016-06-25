package cz.cuni.mff.xrg.odalic.util;

import java.lang.reflect.Array;

public class Arrays {
  private Arrays() {}

  /**
   * @param arrayOfArrays
   * @return
   */
  public static boolean containsNull(Object[][] arrayOfArrays) {
    for (int i = 0; i < arrayOfArrays.length; i++) {
      if (arrayOfArrays[i] == null) {
        return true;
      }
      
      for (int j = 0; j < arrayOfArrays[i].length; j++) {
        if (arrayOfArrays[i][j] == null) {
          return true;
        }
      }
    }
    
    return false;
  }

  /**
   * @param matrix
   * @return
   */
  public static boolean isMatrix(Object[][] matrix) {
    for (int i = 0; i < matrix.length; i++) {
      if (matrix[i].length != matrix.length) {
        return false;
      }
    } 
    
    return true;
  }

  /**
   * @param type
   * @param matrix
   * @return
   */
  public static <T> T[][] deepCopy(Class<T> type, T[][] matrix) {
    final int rows = matrix.length;
    final int columns = (rows > 0) ? (matrix[0].length) : 0; 
    
    @SuppressWarnings("unchecked")
    T[][] copy = (T[][]) Array.newInstance(type, rows, columns);
    for(int i = 0; i < matrix.length; i++) {
        copy[i] = matrix[i].clone();
    }
    
    return copy;
  }
  
  
}

package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.collect.ImmutableMap;

import cz.cuni.mff.xrg.odalic.api.rest.values.ResultValue;
import cz.cuni.mff.xrg.odalic.positions.CellRelationPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;


public final class ResultAdapter extends XmlAdapter<ResultValue, Result> {

  @Override
  public ResultValue marshal(Result bound) throws Exception {
    return new ResultValue(bound);
  }

  @Override
  public Result unmarshal(ResultValue value) throws Exception {
    final ImmutableMap.Builder<ColumnRelationPosition, ColumnRelationAnnotation> columnRelationAnnotationsBuilder = ImmutableMap.builder();
    for (final Map.Entry<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> entry : value.getColumnRelationAnnotations().entrySet()) {
      final ColumnPosition first = entry.getKey();
      
      for (final Map.Entry<ColumnPosition, ColumnRelationAnnotation> innerEntry : entry.getValue().entrySet()) {
        final ColumnPosition second = innerEntry.getKey();
        final ColumnRelationAnnotation annotation = innerEntry.getValue();
        
        columnRelationAnnotationsBuilder.put(new ColumnRelationPosition(first, second), annotation);
      }
    }
    
    final ImmutableMap.Builder<CellRelationPosition, CellRelationAnnotation> cellRelationAnnotationsBuilder = ImmutableMap.builder();
    for (final Map.Entry<RowPosition, Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> entry : value.getCellRelationAnnotations().entrySet()) {
      final RowPosition row = entry.getKey();
      
      for (final Map.Entry<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>> innerEntry : entry.getValue().entrySet()) {
        final ColumnPosition first = innerEntry.getKey();
        
        for (final Map.Entry<ColumnPosition, CellRelationAnnotation> subInnerEntry : innerEntry.getValue().entrySet()) {
          final ColumnPosition second = subInnerEntry.getKey();
          final CellRelationAnnotation annotation = subInnerEntry.getValue();
        
          cellRelationAnnotationsBuilder.put(new CellRelationPosition(new ColumnRelationPosition(first, second), row), annotation);
        }
      }
    }
    
    return new Result(value.getSubjectColumnPosition(), value.getHeaderAnnotations(), value.getCellAnnotations(),
        columnRelationAnnotationsBuilder.build(), cellRelationAnnotationsBuilder.build());
  }
}

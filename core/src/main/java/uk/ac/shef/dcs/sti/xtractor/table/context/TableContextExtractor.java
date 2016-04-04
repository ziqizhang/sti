package uk.ac.shef.dcs.sti.xtractor.table.context;

import org.w3c.dom.Document;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.TContext;

import java.util.List;

/**
 * Created by - on 04/04/2016.
 */
public interface TableContextExtractor {

    List<TContext> extract(String file, Document doc) throws STIException;
}

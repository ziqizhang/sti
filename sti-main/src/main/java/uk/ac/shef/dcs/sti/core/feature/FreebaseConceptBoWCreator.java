package uk.ac.shef.dcs.sti.core.feature;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 18/02/14
 * Time: 16:42
 * To change this template use File | Settings | File Templates.
 */
public class FreebaseConceptBoWCreator implements OntologyBasedBoWCreator {
    @Override
    public List<String> create(String uri) {
        List<String> bow = new ArrayList<>();
        for(String part: uri.split("/")){
            part=part.trim();
            for(String pp: part.split("_")){
                pp = pp.trim();
                if(pp.length()>0)
                    bow.add(pp);
            }
        }
        return bow;
    }
}

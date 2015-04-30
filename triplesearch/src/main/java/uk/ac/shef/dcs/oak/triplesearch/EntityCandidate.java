package uk.ac.shef.dcs.oak.triplesearch;

import uk.ac.shef.dcs.oak.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * to represent a candidate entity for a content cell of a table
 * WARNING: CHANGING THIS CLASS NAME, OR PACKAGE, WILL MAKE EXISTING CACHE OF SUCH OBJECTS OBSOLETE
 */
public class EntityCandidate implements Serializable {
    protected String name;
    protected String id;
    protected List<String[]> types;
    protected List<String[]> facts;

    public EntityCandidate(String id, String name) {
        this.id = id;
        this.name = name;
      /*  if(name.contains("wind instrument"))
            System.out.println("wrong wrong "+id);*/
        types = new ArrayList<String[]>();
        facts = new ArrayList<String[]>();
    }

    public EntityCandidate() {
        types = new ArrayList<String[]>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String[]> getTypes() {
        return types;
    }

    public List<String> getTypeIds() {
        List<String> ids = new ArrayList<String>();
        for (String[] t : types) {
            ids.add(t[0]);
        }
        return ids;
    }

    public List<String> getTypeNames() {
        List<String> ids = new ArrayList<String>();
        for (String[] t : types) {
            ids.add(t[1]);
        }
        return ids;
    }

    public void addType(String[] type) {
        this.types.add(type);
    }

    public boolean hasTypeId(String typeId) {
        for (String[] t : types) {
            if (t[0].equals(typeId))
                return true;
        }
        return false;
    }

    public List<String[]> getFacts() {
        /*if (facts != null&&facts.size()>0) {
            for (String[] ft : facts) {
                if (ft[1].contains("wind instrument")) {
                    String type = "";
                    for (String[] ftt : facts) {
                        if (ftt[0].equals("/type/object/type") && ftt[3].equals("n")) {
                            type = type + "," + ftt[2];
                        }
                    }
                    System.out.println("CULPRIT:" + id + "=" + type);
                    break;
                }
            }
        }
*/
        return facts;
    }

    public void setFacts(List<String[]> facts) {
        /*if (this.getId().equals("/m/06qw_")) {
            String type = "";
            for (String[] ft : facts) {
                if (ft[0].equals("/type/object/type") && ft[3].equals("n")) {
                    type = type + "," + ft[2];
                }
            }

            System.out.println("######## object fact reset " + id + ",=" + type);
        }*/



        List<String[]> deduplicate = new ArrayList<String[]>();
        for (String[] f : facts) {
            if (!CollectionUtils.array_list_contains(deduplicate, f))
                deduplicate.add(f);
        }
        this.facts = deduplicate;



        /*if (facts != null&&facts.size()>0) {
        for (String[] ft : facts) {
            if (ft[1].contains("wind instrument")) {
                String type = "";
                for (String[] ftt : facts) {
                    if (ftt[0].equals("/type/object/type") && ftt[3].equals("n")) {
                        type = type + "," + ftt[2];
                    }
                }
                System.out.println("CULPRIT:" + id + "=" + type);
                break;
            }
        }
        }*/
    }

    public String toString() {
        return getId() + "," + getName();
    }

    public int hashCode() {
        return getId().hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof EntityCandidate) {
            EntityCandidate e = (EntityCandidate) o;
            return e.getId().equals(getId());
        }
        return false;
    }
}

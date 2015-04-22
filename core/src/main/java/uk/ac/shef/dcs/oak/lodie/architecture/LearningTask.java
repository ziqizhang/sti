package uk.ac.shef.dcs.oak.lodie.architecture;

import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 17/10/12
 * Time: 12:44
 *
 * todo: this class isnt complete
 * (should also support: matching each sub-query to a triple pattern)
 *
 * A LearningTask represents the task defined by a user. Following the "airline" example, it is
 *
 * "?a  _isa_        Con:Airline
 *  ?a  _flightto_   Con:US
 *  ?a  _allianceof_ 'star'
 * "
 *
 * The LearningTask should define what is/are the individual LearningJob to be accomplished by IE in order
 * to satisfy this LearningTask. For example, the decomposing process may decides that to answer this query
 * IE needs to learn the following two constrained relations:
 * " ?a  _allianceof_ 'star' "
 * and "[all_airlines_satisfying_the_prev_two_relations] _allianceof_ ?b"
 *
 * so there are two LearningJobs to be accomplished and the ultimate answer will be joint of the individual learning output
 *
 *
 *
 */
public class LearningTask {

    private String description; //NL narrative of the learningtask
    private List<LearningJob> jobs;


                             //

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<LearningJob> getJobs() {
        return jobs;
    }

    public void setJobs(List<LearningJob> jobs) {
        this.jobs = jobs;
    }
}

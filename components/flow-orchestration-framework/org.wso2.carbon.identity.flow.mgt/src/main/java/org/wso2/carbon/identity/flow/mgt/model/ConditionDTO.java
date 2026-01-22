package org.wso2.carbon.identity.flow.mgt.model;

//import static org.wso2.carbon.identity.flow.mgt.Constants.OperatorTypes.EQUALS;
//import static org.wso2.carbon.identity.flow.mgt.Constants.OperatorTypes.NOT_EQUALS;
//import static org.wso2.carbon.identity.flow.mgt.Constants.OperatorTypes.GREATER_THAN;
//import static org.wso2.carbon.identity.flow.mgt.Constants.OperatorTypes.LESS_THAN;
//import static org.wso2.carbon.identity.flow.mgt.Constants.OperatorTypes.GREATER_THAN_OR_EQUALS;
//import static org.wso2.carbon.identity.flow.mgt.Constants.OperatorTypes.LESS_THAN_OR_EQUALS;
//import static org.wso2.carbon.identity.flow.mgt.Constants.OperatorTypes.CLOSED_RANGE;
//import static org.wso2.carbon.identity.flow.mgt.Constants.OperatorTypes.OPEN_RANGE;
//import static org.wso2.carbon.identity.flow.mgt.Constants.OperatorTypes.CONTAINS;
//import static org.wso2.carbon.identity.flow.mgt.Constants.OperatorTypes.NOT_CONTAINS;

import java.io.Serializable;


public class ConditionDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String ruleId;
    private String next;

    public ConditionDTO() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}

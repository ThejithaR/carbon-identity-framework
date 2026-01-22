package org.wso2.carbon.identity.flow.mgt.model;

import org.wso2.carbon.identity.rule.management.api.model.Rule;

public class ConditionRule {
    private String id;
    private String TenetDomain;
    private Rule rule;

    public ConditionRule(String id, String tenetDomain) {

        this.id = id;
        TenetDomain = tenetDomain;
    }


}

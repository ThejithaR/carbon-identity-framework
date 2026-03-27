/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response wrapper for the flow execution context metadata endpoint.
 * <p>
 * Contains the hierarchical tree of context nodes and optional metadata
 * about the flow type and claim dialect used for populating claim children.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowContextMetadataResponse {

    private List<ContextTreeNode> contextTree;
    private String flowType;
    private String claimDialect;

    @JsonProperty("contextTree")
    public List<ContextTreeNode> getContextTree() {

        return contextTree;
    }

    public void setContextTree(List<ContextTreeNode> contextTree) {

        this.contextTree = contextTree;
    }

    public FlowContextMetadataResponse contextTree(List<ContextTreeNode> contextTree) {

        this.contextTree = contextTree;
        return this;
    }

    @JsonProperty("flowType")
    public String getFlowType() {

        return flowType;
    }

    public void setFlowType(String flowType) {

        this.flowType = flowType;
    }

    public FlowContextMetadataResponse flowType(String flowType) {

        this.flowType = flowType;
        return this;
    }

    @JsonProperty("claimDialect")
    public String getClaimDialect() {

        return claimDialect;
    }

    public void setClaimDialect(String claimDialect) {

        this.claimDialect = claimDialect;
    }

    public FlowContextMetadataResponse claimDialect(String claimDialect) {

        this.claimDialect = claimDialect;
        return this;
    }
}

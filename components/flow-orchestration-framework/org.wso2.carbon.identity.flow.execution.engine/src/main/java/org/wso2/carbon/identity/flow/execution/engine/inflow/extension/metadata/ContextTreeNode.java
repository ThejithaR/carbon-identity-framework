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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the FlowExecutionContext metadata tree.
 * <p>
 * Each node describes a field or container in the context hierarchy,
 * annotated with its data type, allowed operations, and behavioral flags
 * that inform the frontend tree component how to render and interact with it.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContextTreeNode {

    /**
     * Node type enum for categorizing tree nodes.
     */
    public enum NodeType {
        OBJECT,
        MAP,
        COMPLEX_MAP,
        LEAF
    }

    private final String key;
    private final String title;
    private final String path;
    private final String dataType;
    private final NodeType nodeType;
    private final List<String> allowedOperations;
    private final boolean readOnly;
    private final boolean replaceable;
    private final boolean canDelete;
    private final boolean dynamicEntryAllowed;
    private final String dynamicEntryType;
    private final List<ContextTreeNode> children;

    private ContextTreeNode(Builder builder) {

        this.key = builder.key;
        this.title = builder.title;
        this.path = builder.path;
        this.dataType = builder.dataType;
        this.nodeType = builder.nodeType;
        this.allowedOperations = builder.allowedOperations;
        this.readOnly = builder.readOnly;
        this.replaceable = builder.replaceable;
        this.canDelete = builder.canDelete;
        this.dynamicEntryAllowed = builder.dynamicEntryAllowed;
        this.dynamicEntryType = builder.dynamicEntryType;
        this.children = builder.children;
    }

    @JsonProperty("key")
    public String getKey() {

        return key;
    }

    @JsonProperty("title")
    public String getTitle() {

        return title;
    }

    @JsonProperty("path")
    public String getPath() {

        return path;
    }

    @JsonProperty("dataType")
    public String getDataType() {

        return dataType;
    }

    @JsonProperty("nodeType")
    public NodeType getNodeType() {

        return nodeType;
    }

    @JsonProperty("allowedOperations")
    public List<String> getAllowedOperations() {

        return allowedOperations;
    }

    @JsonProperty("readOnly")
    public boolean isReadOnly() {

        return readOnly;
    }

    @JsonProperty("replaceable")
    public boolean isReplaceable() {

        return replaceable;
    }

    @JsonProperty("canDelete")
    public boolean isCanDelete() {

        return canDelete;
    }

    @JsonProperty("dynamicEntryAllowed")
    public boolean isDynamicEntryAllowed() {

        return dynamicEntryAllowed;
    }

    @JsonProperty("dynamicEntryType")
    public String getDynamicEntryType() {

        return dynamicEntryType;
    }

    @JsonProperty("children")
    public List<ContextTreeNode> getChildren() {

        return children;
    }

    /**
     * Builder for constructing ContextTreeNode instances.
     */
    public static class Builder {

        private String key;
        private String title;
        private String path;
        private String dataType;
        private NodeType nodeType;
        private List<String> allowedOperations = new ArrayList<>();
        private boolean readOnly;
        private boolean replaceable;
        private boolean canDelete;
        private boolean dynamicEntryAllowed;
        private String dynamicEntryType;
        private List<ContextTreeNode> children;

        public Builder key(String key) {

            this.key = key;
            return this;
        }

        public Builder title(String title) {

            this.title = title;
            return this;
        }

        public Builder path(String path) {

            this.path = path;
            return this;
        }

        public Builder dataType(String dataType) {

            this.dataType = dataType;
            return this;
        }

        public Builder nodeType(NodeType nodeType) {

            this.nodeType = nodeType;
            return this;
        }

        public Builder allowedOperations(List<String> allowedOperations) {

            this.allowedOperations = allowedOperations;
            return this;
        }

        public Builder readOnly(boolean readOnly) {

            this.readOnly = readOnly;
            return this;
        }

        public Builder replaceable(boolean replaceable) {

            this.replaceable = replaceable;
            return this;
        }

        public Builder canDelete(boolean canDelete) {

            this.canDelete = canDelete;
            return this;
        }

        public Builder dynamicEntryAllowed(boolean dynamicEntryAllowed) {

            this.dynamicEntryAllowed = dynamicEntryAllowed;
            return this;
        }

        public Builder dynamicEntryType(String dynamicEntryType) {

            this.dynamicEntryType = dynamicEntryType;
            return this;
        }

        public Builder children(List<ContextTreeNode> children) {

            this.children = children;
            return this;
        }

        public ContextTreeNode build() {

            return new ContextTreeNode(this);
        }
    }
}

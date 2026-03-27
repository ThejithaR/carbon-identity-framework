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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor.HierarchicalPrefixMatcher;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for building the FlowExecutionContext metadata tree.
 * <p>
 * This tree represents the hierarchical structure of the flow execution context,
 * annotated with data types, allowed operations, and behavioral flags. The frontend
 * tree component uses this metadata to render an interactive access configuration editor.
 */
public final class FlowContextMetadataBuilder {

    private static final Log LOG = LogFactory.getLog(FlowContextMetadataBuilder.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, List<String>>> RESTRICTIONS_TYPE_REF =
            new TypeReference<Map<String, List<String>>>() { };

    private static final String RESTRICTIONS_RESOURCE_BASE = "META-INF/flow-context-restrictions/";
    private static final String LOCAL_CLAIM_DIALECT = "http://wso2.org/claims";

    private static final List<String> OPS_EXPOSE_ONLY = Collections.singletonList("EXPOSE");
    private static final List<String> OPS_EXPOSE_MODIFY = Collections.unmodifiableList(
            Arrays.asList("EXPOSE", "MODIFY"));

    // Attribute profile mapping for flow types — used for profile-filtered claim fetching.
    private static final String SELF_REGISTRATION_PROFILE = "selfRegistration";
    private static final String END_USER_PROFILE = "endUser";

    private FlowContextMetadataBuilder() {

        // Utility class, no instantiation.
    }

    /**
     * Build the context metadata tree.
     *
     * @param flowType     Optional flow type for profile-filtered claims and restrictions.
     *                     If null, all local claims are returned with no restrictions.
     * @param tenantDomain The tenant domain for claim resolution.
     * @return FlowContextMetadataResponse containing the context tree.
     * @throws FlowEngineServerException If an error occurs while building the tree.
     */
    public static FlowContextMetadataResponse buildContextTree(String flowType, String tenantDomain)
            throws FlowEngineServerException {

        List<ContextTreeNode> rootNodes = new ArrayList<>();

        // Build /user/ subtree.
        rootNodes.add(buildUserNode(flowType, tenantDomain));

        // Build /flow/ subtree (read-only).
        rootNodes.add(buildFlowNode());

        // Build /graph/ subtree (read-only).
        rootNodes.add(buildGraphNode());

        // Build /input/ map node.
        rootNodes.add(buildInputNode());

        // Build /properties/ complex map node.
        rootNodes.add(buildPropertiesNode());

        // Apply flow-type restrictions if provided.
        if (flowType != null) {
            applyRestrictions(rootNodes, flowType);
        }

        return new FlowContextMetadataResponse()
                .contextTree(rootNodes)
                .flowType(flowType)
                .claimDialect(LOCAL_CLAIM_DIALECT);
    }

    /**
     * Build the /user/ subtree with claims from ClaimMetadataManagementService.
     */
    private static ContextTreeNode buildUserNode(String flowType, String tenantDomain)
            throws FlowEngineServerException {

        List<ContextTreeNode> userChildren = new ArrayList<>();

        // Scalar user fields.
        userChildren.add(buildLeaf("userId", "userId",
                HierarchicalPrefixMatcher.USER_ID_PATH, "String", true, false));
        userChildren.add(buildLeaf("username", "username",
                HierarchicalPrefixMatcher.USER_NAME_PATH, "String", true, false));
        userChildren.add(buildLeaf("userStoreDomain", "userStoreDomain",
                HierarchicalPrefixMatcher.USER_STORE_DOMAIN_PATH, "String", true, false));

        // /user/claims/ map — populated with claim URIs.
        List<ContextTreeNode> claimChildren = fetchClaimNodes(flowType, tenantDomain);
        userChildren.add(new ContextTreeNode.Builder()
                .key("claims")
                .title("claims")
                .path(HierarchicalPrefixMatcher.USER_CLAIMS_PREFIX)
                .dataType("Map<String, String>")
                .nodeType(ContextTreeNode.NodeType.MAP)
                .allowedOperations(OPS_EXPOSE_ONLY)
                .dynamicEntryAllowed(true)
                .dynamicEntryType("String")
                .children(claimChildren)
                .build());

        // /user/credentials/ map.
        userChildren.add(new ContextTreeNode.Builder()
                .key("credentials")
                .title("credentials")
                .path(HierarchicalPrefixMatcher.USER_CREDENTIALS_PREFIX)
                .dataType("Map<String, char[]>")
                .nodeType(ContextTreeNode.NodeType.MAP)
                .allowedOperations(OPS_EXPOSE_ONLY)
                .dynamicEntryAllowed(true)
                .dynamicEntryType("char[]")
                .children(buildCredentialChildren())
                .build());

        // /user/federatedAssociations/ map.
        userChildren.add(new ContextTreeNode.Builder()
                .key("federatedAssociations")
                .title("federatedAssociations")
                .path(HierarchicalPrefixMatcher.USER_FEDERATED_PREFIX)
                .dataType("Map<String, String>")
                .nodeType(ContextTreeNode.NodeType.MAP)
                .allowedOperations(OPS_EXPOSE_ONLY)
                .dynamicEntryAllowed(true)
                .dynamicEntryType("String")
                .children(new ArrayList<>())
                .build());

        return new ContextTreeNode.Builder()
                .key("user")
                .title("user")
                .path(HierarchicalPrefixMatcher.USER_PREFIX)
                .dataType("FlowUser")
                .nodeType(ContextTreeNode.NodeType.OBJECT)
                .allowedOperations(OPS_EXPOSE_ONLY)
                .children(userChildren)
                .build();
    }

    /**
     * Fetch claim URIs as tree nodes from ClaimMetadataManagementService.
     */
    private static List<ContextTreeNode> fetchClaimNodes(String flowType, String tenantDomain)
            throws FlowEngineServerException {

        ClaimMetadataManagementService claimService = FlowExecutionEngineDataHolder.getInstance()
                .getClaimMetadataManagementService();
        if (claimService == null) {
            LOG.warn("ClaimMetadataManagementService is not available. Returning empty claims list.");
            return new ArrayList<>();
        }

        List<ContextTreeNode> claimNodes = new ArrayList<>();
        try {
            List<LocalClaim> claims;
            if (flowType != null) {
                String profile = resolveAttributeProfile(flowType);
                claims = claimService.getSupportedLocalClaimsForProfile(tenantDomain, profile);
            } else {
                claims = claimService.getLocalClaims(tenantDomain);
            }

            for (LocalClaim claim : claims) {
                String claimURI = claim.getClaimURI();
                String claimPath = HierarchicalPrefixMatcher.USER_CLAIMS_PREFIX + claimURI;
                claimNodes.add(new ContextTreeNode.Builder()
                        .key("claim-" + claimURI.hashCode())
                        .title(claimURI)
                        .path(claimPath)
                        .dataType("String")
                        .nodeType(ContextTreeNode.NodeType.LEAF)
                        .allowedOperations(OPS_EXPOSE_MODIFY)
                        .canDelete(true)
                        .build());
            }
        } catch (ClaimMetadataException e) {
            throw new FlowEngineServerException("FE-65030",
                    "Error retrieving claims for context metadata.",
                    "Failed to retrieve local claims for tenant: " + tenantDomain, e);
        }
        return claimNodes;
    }

    /**
     * Build pre-populated credential children (currently only "password").
     */
    private static List<ContextTreeNode> buildCredentialChildren() {

        List<ContextTreeNode> children = new ArrayList<>();
        children.add(new ContextTreeNode.Builder()
                .key("credential-password")
                .title("password")
                .path(HierarchicalPrefixMatcher.USER_CREDENTIALS_PREFIX + "password")
                .dataType("char[]")
                .nodeType(ContextTreeNode.NodeType.LEAF)
                .allowedOperations(OPS_EXPOSE_MODIFY)
                .canDelete(true)
                .build());
        return children;
    }

    /**
     * Build the /flow/ subtree (read-only metadata).
     */
    private static ContextTreeNode buildFlowNode() {

        List<ContextTreeNode> flowChildren = new ArrayList<>();
        flowChildren.add(buildReadOnlyLeaf("tenantDomain", "tenantDomain",
                HierarchicalPrefixMatcher.FLOW_TENANT_PATH, "String"));
        flowChildren.add(buildReadOnlyLeaf("applicationId", "applicationId",
                HierarchicalPrefixMatcher.FLOW_APP_ID_PATH, "String"));
        flowChildren.add(buildReadOnlyLeaf("flowType", "flowType",
                HierarchicalPrefixMatcher.FLOW_TYPE_PATH, "String"));
        flowChildren.add(buildReadOnlyLeaf("contextIdentifier", "contextIdentifier",
                HierarchicalPrefixMatcher.FLOW_PREFIX + "contextIdentifier", "String"));

        return new ContextTreeNode.Builder()
                .key("flow")
                .title("flow")
                .path(HierarchicalPrefixMatcher.FLOW_PREFIX)
                .dataType("FlowMetadata")
                .nodeType(ContextTreeNode.NodeType.OBJECT)
                .allowedOperations(OPS_EXPOSE_ONLY)
                .readOnly(true)
                .children(flowChildren)
                .build();
    }

    /**
     * Build the /graph/ subtree (read-only graph state).
     */
    private static ContextTreeNode buildGraphNode() {

        List<ContextTreeNode> currentNodeChildren = new ArrayList<>();
        currentNodeChildren.add(buildReadOnlyLeaf("id", "id",
                HierarchicalPrefixMatcher.GRAPH_CURRENT_NODE_PREFIX + "id", "String"));
        currentNodeChildren.add(buildReadOnlyLeaf("type", "type",
                HierarchicalPrefixMatcher.GRAPH_CURRENT_NODE_PREFIX + "type", "String"));

        ContextTreeNode currentNode = new ContextTreeNode.Builder()
                .key("currentNode")
                .title("currentNode")
                .path(HierarchicalPrefixMatcher.GRAPH_CURRENT_NODE_PREFIX)
                .dataType("NodeConfig")
                .nodeType(ContextTreeNode.NodeType.OBJECT)
                .allowedOperations(OPS_EXPOSE_ONLY)
                .readOnly(true)
                .children(currentNodeChildren)
                .build();

        List<ContextTreeNode> graphChildren = new ArrayList<>();
        graphChildren.add(currentNode);

        return new ContextTreeNode.Builder()
                .key("graph")
                .title("graph")
                .path(HierarchicalPrefixMatcher.GRAPH_PREFIX)
                .dataType("GraphState")
                .nodeType(ContextTreeNode.NodeType.OBJECT)
                .allowedOperations(OPS_EXPOSE_ONLY)
                .readOnly(true)
                .children(graphChildren)
                .build();
    }

    /**
     * Build the /input/ map node (runtime extensible user input data).
     */
    private static ContextTreeNode buildInputNode() {

        return new ContextTreeNode.Builder()
                .key("input")
                .title("input")
                .path(HierarchicalPrefixMatcher.INPUT_PREFIX)
                .dataType("Map<String, String>")
                .nodeType(ContextTreeNode.NodeType.MAP)
                .allowedOperations(OPS_EXPOSE_ONLY)
                .dynamicEntryAllowed(true)
                .dynamicEntryType("String")
                .children(new ArrayList<>())
                .build();
    }

    /**
     * Build the /properties/ complex map node (fully extensible flow properties).
     */
    private static ContextTreeNode buildPropertiesNode() {

        return new ContextTreeNode.Builder()
                .key("properties")
                .title("properties")
                .path(HierarchicalPrefixMatcher.PROPERTIES_PREFIX)
                .dataType("Map<String, Object>")
                .nodeType(ContextTreeNode.NodeType.COMPLEX_MAP)
                .allowedOperations(OPS_EXPOSE_ONLY)
                .dynamicEntryAllowed(true)
                .dynamicEntryType("Object")
                .children(new ArrayList<>())
                .build();
    }

    /**
     * Build a writable leaf node (supports EXPOSE and REPLACE).
     */
    private static ContextTreeNode buildLeaf(String key, String title, String path,
                                             String dataType, boolean replaceable, boolean readOnly) {

        List<String> ops = replaceable ? OPS_EXPOSE_MODIFY : OPS_EXPOSE_ONLY;
        return new ContextTreeNode.Builder()
                .key(key)
                .title(title)
                .path(path)
                .dataType(dataType)
                .nodeType(ContextTreeNode.NodeType.LEAF)
                .allowedOperations(ops)
                .replaceable(replaceable)
                .readOnly(readOnly)
                .build();
    }

    /**
     * Build a read-only leaf node (EXPOSE only).
     */
    private static ContextTreeNode buildReadOnlyLeaf(String key, String title, String path, String dataType) {

        return new ContextTreeNode.Builder()
                .key(key)
                .title(title)
                .path(path)
                .dataType(dataType)
                .nodeType(ContextTreeNode.NodeType.LEAF)
                .allowedOperations(OPS_EXPOSE_ONLY)
                .readOnly(true)
                .build();
    }

    /**
     * Apply flow-type restrictions by loading the blacklist JSON and pruning excluded paths from the tree.
     */
    private static void applyRestrictions(List<ContextTreeNode> rootNodes, String flowType) {

        Set<String> excludedPaths = loadExcludedPaths(flowType);
        if (excludedPaths.isEmpty()) {
            return;
        }
        pruneExcludedNodes(rootNodes, excludedPaths);
    }

    /**
     * Load excluded paths from the flow-type restriction JSON config file.
     */
    private static Set<String> loadExcludedPaths(String flowType) {

        String resourcePath = RESTRICTIONS_RESOURCE_BASE + flowType + ".json";
        try (InputStream inputStream = FlowContextMetadataBuilder.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No restriction config found for flow type: " + flowType +
                            ". Returning full context tree.");
                }
                return Collections.emptySet();
            }

            Map<String, List<String>> config = OBJECT_MAPPER.readValue(inputStream, RESTRICTIONS_TYPE_REF);
            List<String> excluded = config.get("excludedPaths");
            return excluded != null ? new HashSet<>(excluded) : Collections.emptySet();
        } catch (IOException e) {
            LOG.error("Error reading flow context restriction config for flow type: " + flowType, e);
            return Collections.emptySet();
        }
    }

    /**
     * Recursively prune nodes whose paths match any of the excluded path prefixes.
     * A node is removed if its path starts with an excluded prefix, or if an excluded
     * prefix starts with the node's path (i.e., the entire subtree is excluded).
     */
    private static void pruneExcludedNodes(List<ContextTreeNode> nodes, Set<String> excludedPaths) {

        Iterator<ContextTreeNode> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            ContextTreeNode node = iterator.next();
            String nodePath = node.getPath();

            // Check if this node should be removed.
            boolean shouldRemove = false;
            for (String excludedPath : excludedPaths) {
                if (nodePath != null && (nodePath.startsWith(excludedPath) || excludedPath.startsWith(nodePath)
                        && nodePath.equals(excludedPath))) {
                    shouldRemove = true;
                    break;
                }
            }

            if (shouldRemove) {
                iterator.remove();
            } else if (node.getChildren() != null) {
                // Recurse into children.
                pruneExcludedNodes(node.getChildren(), excludedPaths);
            }
        }
    }

    /**
     * Resolve the attribute profile for a given flow type.
     * Maps internal flow type identifiers to claim attribute profile names.
     */
    private static String resolveAttributeProfile(String flowType) {

        switch (flowType) {
            case "REGISTRATION":
            case "INVITED_USER_REGISTRATION":
                return SELF_REGISTRATION_PROFILE;
            case "PASSWORD_RECOVERY":
                return END_USER_PROFILE;
            default:
                return SELF_REGISTRATION_PROFILE;
        }
    }
}

/*
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package com.uber.cadence.samples.query;

import com.uber.cadence.samples.common.SampleConstants;

/** Shared settings for Custom Workflow Controls (Markdoc query) samples. */
public final class QueryConstants {

  private QueryConstants() {}

  /** Task list used by {@link QueryWorker} and all query sample starters. */
  public static final String TASK_LIST = "query";

  /** Cadence domain; must match Markdoc {% signal %} / {% start %} attributes. */
  public static final String DOMAIN = SampleConstants.DOMAIN;

  /**
   * Cluster name for Cadence Web Markdoc controls. Must match the cluster configured in your
   * Cadence Web deployment (v4.0.14+).
   */
  public static final String CLUSTER = "cluster0";

  /** Registered workflow type for {@link MarkdownQueryWorkflow}. */
  public static final String MARKDOWN_QUERY_WORKFLOW_TYPE = "MarkdownQueryWorkflow";

  /**
   * Markdoc {@code signalName} for {@link MarkdownQueryWorkflow.WorkflowIface#complete(boolean)}.
   *
   * <p>The Cadence Java SDK registers workflow signals as {@code InterfaceName::methodName} (here
   * {@code WorkflowIface::complete}). Cadence Web sends the raw string from the Markdoc template,
   * so the {@code signalName} attribute in the markdown must use this qualified form.
   */
  public static final String MARKDOWN_QUERY_COMPLETE_SIGNAL_MARKDOC = "WorkflowIface::complete";

  /** Registered workflow type for {@link LunchVoteWorkflow}. */
  public static final String LUNCH_VOTE_WORKFLOW_TYPE = "LunchVoteWorkflow";

  /** Registered workflow type for {@link OrderFulfillmentWorkflow}. */
  public static final String ORDER_FULFILLMENT_WORKFLOW_TYPE = "OrderFulfillmentWorkflow";

  /** Registered workflow type for {@link ExpenseReportWorkflow}. */
  public static final String EXPENSE_REPORT_WORKFLOW_TYPE = "ExpenseReportWorkflow";
}

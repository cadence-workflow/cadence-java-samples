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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The JSON shape Cadence Web (v4.0.14+) expects for formatted query responses. When a {@link
 * com.uber.cadence.workflow.QueryMethod} returns this object, the default data converter serializes
 * it to:
 *
 * <pre>{@code
 * {
 *   "cadenceResponseType": "formattedData",
 *   "format": "text/markdown",
 *   "data": "<your markdown string>",
 *   "title": "<optional page title>"
 * }
 * }</pre>
 *
 * Cadence Web detects this shape and renders the {@code data} field as markdown (with Markdoc
 * extensions) instead of displaying raw JSON.
 */
public final class MarkdownFormattedResponse {

  private final String cadenceResponseType;
  private final String format;
  private final String data;
  private final String title;

  /**
   * Constructor for JSON deserialization (e.g. Java clients using the Cadence SDK data converter).
   * A no-arg constructor cannot populate {@code private final} fields; {@code @JsonCreator} with
   * explicit properties is required.
   */
  @JsonCreator
  private MarkdownFormattedResponse(
      @JsonProperty("cadenceResponseType") String cadenceResponseType,
      @JsonProperty("format") String format,
      @JsonProperty("data") String data,
      @JsonProperty("title") String title) {
    this.cadenceResponseType = cadenceResponseType;
    this.format = format;
    this.data = data;
    this.title = title;
  }

  /**
   * @param markdownData the markdown string to render in Cadence Web. May include Markdoc tags such
   *     as {@code {%- signal -%}} and {@code {%- start -%}}.
   */
  public MarkdownFormattedResponse(String markdownData) {
    // These two values are the required constants that Cadence Web checks for.
    this(markdownData, null);
  }

  /**
   * @param markdownData the markdown string to render in Cadence Web
   * @param title optional title that the Cadence Web Home tab shows; may be null
   */
  public MarkdownFormattedResponse(String markdownData, String title) {
    this("formattedData", "text/markdown", markdownData, title);
  }

  public String getCadenceResponseType() {
    return cadenceResponseType;
  }

  public String getFormat() {
    return format;
  }

  public String getData() {
    return data;
  }

  public String getTitle() {
    return title;
  }
}

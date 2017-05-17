/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.legacy.rest.action.template;

import org.elasticsearch.legacy.action.delete.DeleteResponse;
import org.elasticsearch.legacy.client.Client;
import org.elasticsearch.legacy.common.inject.Inject;
import org.elasticsearch.legacy.common.lucene.uid.Versions;
import org.elasticsearch.legacy.common.settings.Settings;
import org.elasticsearch.legacy.common.xcontent.XContentBuilder;
import org.elasticsearch.legacy.common.xcontent.XContentBuilderString;
import org.elasticsearch.legacy.rest.*;
import org.elasticsearch.legacy.rest.action.support.RestBuilderListener;
import org.elasticsearch.legacy.script.ScriptService;

import static org.elasticsearch.legacy.rest.RestRequest.Method.DELETE;
import static org.elasticsearch.legacy.rest.RestStatus.OK;
import static org.elasticsearch.legacy.rest.RestStatus.NOT_FOUND;

public class RestDeleteSearchTemplateAction extends BaseRestHandler {

    private ScriptService scriptService;

    @Inject
    public RestDeleteSearchTemplateAction(Settings settings, Client client, RestController controller, ScriptService scriptService) {
        super(settings, client);
        controller.registerHandler(DELETE, "/_search/template/{id}", this);
        this.scriptService = scriptService;
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, Client client) {
        final String id = request.param("id");
        final long version = request.paramAsLong("version", Versions.MATCH_ANY);
        scriptService.deleteScriptFromIndex(client,"mustache", id, version, new RestBuilderListener<DeleteResponse>(channel) {
            @Override
            public RestResponse buildResponse(DeleteResponse result, XContentBuilder builder) throws Exception {
                builder.startObject()
                        .field(Fields.FOUND, result.isFound())
                        .field(Fields._INDEX, result.getIndex())
                        .field(Fields._TYPE, result.getType())
                        .field(Fields._ID, result.getId())
                        .field(Fields._VERSION, result.getVersion())
                        .endObject();
                RestStatus status = OK;
                if (!result.isFound()) {
                    status = NOT_FOUND;
                }
                return new BytesRestResponse(status, builder);
            }
        });
    }

    static final class Fields {
        static final XContentBuilderString FOUND = new XContentBuilderString("found");
        static final XContentBuilderString _INDEX = new XContentBuilderString("_index");
        static final XContentBuilderString _TYPE = new XContentBuilderString("_type");
        static final XContentBuilderString _ID = new XContentBuilderString("_id");
        static final XContentBuilderString _VERSION = new XContentBuilderString("_version");
    }


}
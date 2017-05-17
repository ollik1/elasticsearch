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

package org.elasticsearch.legacy.cluster.routing.allocation.allocator;

import org.elasticsearch.legacy.cluster.routing.MutableShardRouting;
import org.elasticsearch.legacy.cluster.routing.RoutingNode;
import org.elasticsearch.legacy.cluster.routing.allocation.FailedRerouteAllocation;
import org.elasticsearch.legacy.cluster.routing.allocation.RoutingAllocation;
import org.elasticsearch.legacy.cluster.routing.allocation.StartedRerouteAllocation;
import org.elasticsearch.legacy.common.component.AbstractComponent;
import org.elasticsearch.legacy.common.inject.Inject;
import org.elasticsearch.legacy.common.settings.ImmutableSettings;
import org.elasticsearch.legacy.common.settings.Settings;
import org.elasticsearch.legacy.gateway.none.NoneGatewayAllocator;

/**
 * The {@link ShardsAllocator} class offers methods for allocating shard within a cluster.
 * These methods include moving shards and re-balancing the cluster. It also allows management
 * of shards by their state. 
 */
public class ShardsAllocators extends AbstractComponent implements ShardsAllocator {

    private final GatewayAllocator gatewayAllocator;
    private final ShardsAllocator allocator;

    public ShardsAllocators() {
        this(ImmutableSettings.Builder.EMPTY_SETTINGS);
    }

    public ShardsAllocators(Settings settings) {
      this(settings, new NoneGatewayAllocator(), new BalancedShardsAllocator(settings));
    }

    @Inject
    public ShardsAllocators(Settings settings, GatewayAllocator gatewayAllocator, ShardsAllocator allocator) {
        super(settings);
        this.gatewayAllocator = gatewayAllocator;
        this.allocator = allocator;
    }

    @Override
    public void applyStartedShards(StartedRerouteAllocation allocation) {
        gatewayAllocator.applyStartedShards(allocation);
        allocator.applyStartedShards(allocation);
    }

    @Override
    public void applyFailedShards(FailedRerouteAllocation allocation) {
        gatewayAllocator.applyFailedShards(allocation);
        allocator.applyFailedShards(allocation);
    }

    @Override
    public boolean allocateUnassigned(RoutingAllocation allocation) {
        boolean changed = false;
        changed |= gatewayAllocator.allocateUnassigned(allocation);
        changed |= allocator.allocateUnassigned(allocation);
        return changed;
    }

    @Override
    public boolean rebalance(RoutingAllocation allocation) {
        return allocator.rebalance(allocation);
    }

    @Override
    public boolean move(MutableShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
        return allocator.move(shardRouting, node, allocation);
    }
}

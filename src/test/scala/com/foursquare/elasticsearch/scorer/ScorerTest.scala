package com.foursquare.elasticsearch.scorer;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.geo.GeoDistance;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import org.junit.Test
import org.junit._

import java.util.Arrays;

import org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers.closeTo;
import org.hamcrest.Matchers.equalTo;

import scala.math;
/**
 */
class DistanceScoreMagicSearchScriptTests {

    @Test
    def simpleUrlScoreTest() {
        val settings: Settings = ImmutableSettings.settingsBuilder()
                .put("gateway.type", "none")
                .put("index.number_of_shards", 1)
                .build();
        val node: Node = NodeBuilder.nodeBuilder().settings(settings).node();
        val mapping: String = XContentFactory.jsonBuilder().startObject().startObject("type1")
                .startObject("properties").startObject("point").field("type", "geo_point").endObject().endObject()
                .endObject().endObject().string();
        node.client().admin().indices().prepareCreate("test").addMapping("type1", mapping).execute().actionGet();

        node.client().prepareIndex("test", "type1", "1")
                .setSource(XContentFactory.jsonBuilder().startObject()
                        .startObject("point").field("lat", 40.7143528).field("lon", -74.0059731).endObject()
                        .field("decayedPopularity1", 3)
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        val searchResponse: SearchResponse = node.client().prepareSearch("test")
                .setQuery(QueryBuilders.customScoreQuery(QueryBuilders.matchAllQuery())
                        .script("distance_score_magic").lang("native").param("lat", 40.759011).param("lon", -73.9844722).param("weight1", 2000).param("weight2", 0.03))
                .execute().actionGet();

        assertThat(Arrays.toString(searchResponse.shardFailures().asInstanceOf[Array[Object]]), searchResponse.failedShards(), equalTo(0));
        assertThat(searchResponse.hits().totalHits(), equalTo(1l));

        // we rely here on score being 1 since we are using match_all
        val distance: Double = GeoDistance.PLANE.calculate(40.7143528, -74.0059731, 40.759011, -73.9844722, DistanceUnit.KILOMETERS);
        assertThat(searchResponse.hits().getAt(0).score().toDouble.asInstanceOf[java.lang.Double],
                closeTo(((1 + 2000 * math.pow(((1.0 * (math.pow(distance, 2.0))) + 1.0), -1.0)
                        * 3 * 0.03).asInstanceOf[java.lang.Double]),
                        0.00001.asInstanceOf[java.lang.Double]));

        node.close();
    }
}

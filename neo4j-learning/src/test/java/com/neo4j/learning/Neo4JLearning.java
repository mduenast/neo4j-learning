package com.neo4j.learning;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author naluem
 */
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.junit.Neo4jRule;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.neo4j.driver.v1.Values.parameters;

public class Neo4JLearning {

    // This rule starts a Neo4j instance for us
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            // This is the Procedure we want to test
            .withProcedure(FullTextIndex.class);

    @Test
    public void shouldAllowIndexingAndFindingANode() throws Throwable {
        // In a try-block, to make sure we close the driver after the test
        try (Driver driver = GraphDatabase.driver(neo4j.boltURI(), Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig())) {

            // Given I've started Neo4j with the FullTextIndex procedure class
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // And given I have a node in the database
            long nodeId = session.run("CREATE (p:User {name:'Brookreson'}) RETURN id(p)")
                    .single()
                    .get(0).asLong();

            // When I use the index procedure to index a node
            session.run("CALL example.index({id}, ['name'])", parameters("id", nodeId));

            // Then I can search for that node with lucene query syntax
            StatementResult result = session.run("CALL example.search('User', 'name:Brook*')");
            assertThat(result.single().get("nodeId").asLong(), equalTo(nodeId));
        }
    }
}

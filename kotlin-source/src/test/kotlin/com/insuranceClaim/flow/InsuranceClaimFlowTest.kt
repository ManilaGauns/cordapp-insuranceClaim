package com.insuranceClaim.flow

import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class InsuranceClaimFlowTest {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    val fname= "PartyA"
    val lname= "Test"
    val address="Mumbai, India"
    val insuranceID = "ID123"
    val type="Life Insurance"
    val value = 30000
    val reason = "Test application"
    val approvedamount=0
    val insuranceStatus="RECEIVED"

    @Before
    fun setup(){
        network= MockNetwork(listOf("com.insuranceClaim.contract"))
        a= network.createPartyNode()
        b= network.createPartyNode()

        listOf(a,b).forEach{it.registerInitiatedFlow(ApplyClaimFlow.Acceptor::class.java)}
        network.runNetwork()
    }

    @After
    fun TearDown(){
        network.stopNodes()
    }

    @Test
    fun `Transaction is Signed by initiator`(){
        val flow= ApplyClaimFlow.ClaimInitiator(b.info.singleIdentity(),10,"",fname,lname,address,insuranceID,type,insuranceStatus)
        val future=a.startFlow(flow)
        network.runNetwork()
        val signedTx=future.getOrThrow()
        signedTx.verifySignaturesExcept(b.info.singleIdentity().owningKey)
    }

    @Test
    fun `Transaction is Signed by Acceptor`(){
        val flow= ApplyClaimFlow.ClaimInitiator(b.info.singleIdentity(),10,"",fname,lname,address,insuranceID,type,insuranceStatus)
        val future=a.startFlow(flow)
        network.runNetwork()
        val signedTx=future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Flow records transaction in both parties vault`(){
        val flow= ApplyClaimFlow.ClaimInitiator(b.info.singleIdentity(),10,"",fname,lname,address,insuranceID,type,insuranceStatus)
        val future=a.startFlow(flow)
        network.runNetwork()
        val signedTx=future.getOrThrow()
        for (node in listOf(a,b)){
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
    }

}
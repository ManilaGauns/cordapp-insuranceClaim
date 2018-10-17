package com.insuranceClaim.contract

import com.insuranceClaim.contract.ClaimContract.Companion.CLAIM_CONTRACT_ID
import com.insuranceClaim.state.ClaimState
import com.insuranceClaim.state.UnderwritingState
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class ClaimContractTest {

    private val ledgerServices = MockServices()
    private val applicantA = TestIdentity(CordaX500Name("ApplicantA", "London", "GB"))
    private val InsurerA = TestIdentity(CordaX500Name("InsurerA", "New York", "US"))
    private val underwriterA = TestIdentity(CordaX500Name("UnderwriterA", "Paris", "FR"))
    val fname= "PartyA"
    val lname= "Test"
    val address="Mumbai, India"
    val insuranceID = "ID123"
    val type="Life Insurance"
    val value = 30000
    val reason = "Test application"
    val approvedamount=0
    val insuranceStatus="RECEIVED"
    val referenceID= "Not Defined"

    @Test
    fun `Transaction must include InsuranceApplication command`(){
        ledgerServices.ledger{
            transaction{
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimApplication())
                verifies()
            }
        }
    }

    @Test
    fun `Create Claim transaction must have zero input`(){
        ledgerServices.ledger{
            transaction{
                input(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimApplication())
                `fails with`("No inputs should be consumed when creating Claim Application.")
            }
        }
    }

    @Test
    fun `Claim transaction must have only one output state`(){
        ledgerServices.ledger{
            transaction{
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimApplication())
                `fails with`("Only one output state should be created.")
            }
        }
    }

    @Test
    fun `Applicant party is not same as Insurance Party`(){
        ledgerServices.ledger{
            transaction{
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,applicantA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimApplication())
                `fails with`("The Applicant and the Insurance Company cannot be the same entity.")
            }
            transaction{
                output(CLAIM_CONTRACT_ID, ClaimState(InsurerA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimApplication())
                `fails with`("The Applicant and the Insurance Company cannot be the same entity.")
            }
        }
    }

    @Test
    fun `All Participants must sign the transaction`(){
        ledgerServices.ledger{
            transaction{
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(applicantA.publicKey, ClaimContract.Commands.ClaimApplication())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(InsurerA.publicKey, ClaimContract.Commands.ClaimApplication())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, applicantA.publicKey), ClaimContract.Commands.ClaimApplication())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(InsurerA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimApplication())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimApplication())
                verifies()
            }
        }
    }

    @Test
    fun `Claim value must be non-negative`(){
        var dummyValue=-2
        var zeroValue=0
        ledgerServices.ledger{
            transaction{
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,dummyValue,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimApplication())
                `fails with`("The Claim value must be non-negative.")
            }
            transaction{
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,zeroValue,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimApplication())
                `fails with`("The Claim value must be non-negative.")
            }
            transaction{
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimApplication())
                verifies()
            }
        }
    }

    @Test
    fun `Transaction must include CompanyResponse command`(){
        ledgerServices.ledger{
            transaction{
                input(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimResponse())
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingStatus())
                verifies()
            }
        }
    }

    @Test
    fun `Company Response transaction must have Two output states`(){
        ledgerServices.ledger{
            transaction{
                input(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimResponse())
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingStatus())
                `fails with`("Two output states should be created.")
            }
        }
    }

    @Test
    fun `Applicant party is not same as the Insurance Party`(){
        ledgerServices.ledger{
            transaction{
                input(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,applicantA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimResponse())
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingStatus())
                `fails with`("The Applicant and the Insurance Company cannot be the same entity.")
            }
            transaction{
                input(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(CLAIM_CONTRACT_ID, ClaimState(InsurerA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimResponse())
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingStatus())
                `fails with`("The Applicant and the Insurance Company cannot be the same entity.")
            }
        }
    }

    @Test
    fun `All Participants must sign the transaction `(){
        ledgerServices.ledger{
            transaction{
                input(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey), ClaimContract.Commands.ClaimResponse())
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingStatus())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                input(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(InsurerA.publicKey), ClaimContract.Commands.ClaimResponse())
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingStatus())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                input(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, applicantA.publicKey), ClaimContract.Commands.ClaimResponse())
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingStatus())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                input(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(InsurerA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimResponse())
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingStatus())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                input(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimResponse())
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingStatus())
                verifies()
            }
        }
    }

}
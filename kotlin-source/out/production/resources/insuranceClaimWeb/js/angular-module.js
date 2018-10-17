"use strict";

// --------
// WARNING:
// --------

// THIS CODE IS ONLY MADE AVAILABLE FOR DEMONSTRATION PURPOSES AND IS NOT SECURE!
// DO NOT USE IN PRODUCTION!

// FOR SECURITY REASONS, USING A JAVASCRIPT WEB APP HOSTED VIA THE CORDA NODE IS
// NOT THE RECOMMENDED WAY TO INTERFACE WITH CORDA NODES! HOWEVER, FOR THIS
// PRE-ALPHA RELEASE IT'S A USEFUL WAY TO EXPERIMENT WITH THE PLATFORM AS IT ALLOWS
// YOU TO QUICKLY BUILD A UI FOR DEMONSTRATION PURPOSES.

// GOING FORWARD WE RECOMMEND IMPLEMENTING A STANDALONE WEB SERVER THAT AUTHORISES
// VIA THE NODE'S RPC INTERFACE. IN THE COMING WEEKS WE'LL WRITE A TUTORIAL ON
// HOW BEST TO DO THIS.

const app = angular.module('demoAppModule', ['ui.bootstrap']);

// Fix for unhandled rejections bug.
app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller('DemoAppController', function($http, $location, $uibModal) {
    const demoApp = this;
    demoApp.showButton=true;

    // We identify the node.
    const apiBaseURL = "/api/claim/";
    let peers = [];
    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);
    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    demoApp.openModal = () => {
        const modalInstance = $uibModal.open({
            templateUrl: 'demoAppModal.html',
            controller: 'ModalInstanceCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                demoApp: () => demoApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        modalInstance.result.then(() => {}, () => {});
    };

    demoApp.openUnderwritingModal = (insurance) => {
        demoApp.currentInsurance = insurance;
        const modalInstance = $uibModal.open({
            templateUrl: 'demoAppUnderwritingModal.html',
            controller: 'ModalInstanceUnderwritingCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                demoApp: () => demoApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

         modalInstance.result.then(() => {}, () => {});
    };

    demoApp.openUnderwritingEvaluationModal = (application) => {
        demoApp.currentApplication = application;
        const modalInstance = $uibModal.open({
            templateUrl:'demoAppUnderwritingEvaluation.html',
            controller: 'ModalInstanceUnderwritingEvaluationCtrl',
            controllerAs: 'modalInstance',
            resolve:{
                demoApp: () => demoApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        modalInstance.result.then(() => {}, () => {});
    };

        demoApp.openCompanyResponseModal = (response) => {
            demoApp.currentResponse = response;
            const modalInstance = $uibModal.open({
                templateUrl:'demoAppCompanyResponse.html',
                controller: 'ModalInstanceCompanyResponseCtrl',
                controllerAs: 'modalInstance',
                resolve:{
                    demoApp: () => demoApp,
                    apiBaseURL: () => apiBaseURL,
                    peers: () => peers
                }
            });

            modalInstance.result.then(() => {}, () => {});
        };

    demoApp.getApplicationStates = () => $http.get(apiBaseURL + "ApplicationStates")
        .then((response) => demoApp.ApplicationStates = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());

    demoApp.getApplicationStates();

    demoApp.getUnderwritingStates = () => $http.get(apiBaseURL + "UnderwritingStates")
            .then((response) => demoApp.UnderwritingStates = Object.keys(response.data)
                .map((key) => response.data[key].state.data)
                .reverse());

    demoApp.getUnderwritingStates();
});

app.controller('ModalInstanceCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    // Validate and create Claim.
    modalInstance.create = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();

            const createInsuranceEndpoint = `${apiBaseURL}create-insurance?fname=${modalInstance.form.fname}&lname=${modalInstance.form.lname}&insuranceID=${modalInstance.form.insuranceID}&type=${modalInstance.form.type}&address=${modalInstance.form.address}&value=${modalInstance.form.value}&reason=${modalInstance.form.reason}`;

            // Create Claim and handle success / fail responses.
            $http.put(createInsuranceEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    demoApp.getApplicationStates();
                },
                (result) => {
                    modalInstance.displayMessage(result);
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    // Close create Claim modal dialogue.
    modalInstance.cancel = () =>
    {
        $uibModalInstance.dismiss();
        demoApp.showButton=true;
    }

    // Validate the Claim.
    function invalidFormInput() {
        return isNaN(modalInstance.form.value) || (modalInstance.form.insuranceID === undefined)|| (modalInstance.form.fname === undefined) || (modalInstance.form.lname === undefined) || (modalInstance.form.address === undefined);
    }
});

// Insurance Underwriting Check controller begins here
app.controller('ModalInstanceUnderwritingCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;
    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;
    modalInstance.currentInsurance=demoApp.currentInsurance;

    modalInstance.createUnderwriting = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;
            $uibModalInstance.close();
            demoApp.showButton=false;

            const createInsuranceUnderwritingEndpoint = `${apiBaseURL}insurance-underwriting?fname=${modalInstance.currentInsurance.fname}&lname=${modalInstance.currentInsurance.lname}&insuranceID=${modalInstance.currentInsurance.insuranceID}&type=${modalInstance.currentInsurance.type}&value=${modalInstance.currentInsurance.value}&reason=${modalInstance.currentInsurance.reason}&insuranceStatus=${modalInstance.currentInsurance.insuranceStatus}&claimID=${modalInstance.currentInsurance.linearId.id}`;

            // Create Claim and handle success / fail responses.
            $http.put(createInsuranceUnderwritingEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    demoApp.getApplicationStates();
                    demoApp.getUnderwritingStates();
                },
                (result) => {
                    modalInstance.displayMessage(result);
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    // Close create Claim modal dialogue.
    modalInstance.cancel = () => $uibModalInstance.dismiss();

    // Validate the Claim.
    function invalidFormInput() {
        return (modalInstance.currentInsurance.fname === undefined) || (modalInstance.currentInsurance.lname === undefined);
    }
});
// Insurance underwriting check controller ends here

//Underwriting Evaluation controller starts here
app.controller('ModalInstanceUnderwritingEvaluationCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;
    modalInstance.currentApplication=demoApp.currentApplication;

    // Underwriting Evaluation.
    modalInstance.underwritingEvaluation = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();

            const createUnderwritingEvaluationEndpoint = `${apiBaseURL}underwriting-evaluation?fname=${modalInstance.currentApplication.fname}&lname=${modalInstance.currentApplication.lname}&insuranceStatus=${modalInstance.currentApplication.insuranceStatus}&referenceID=${modalInstance.currentApplication.linearId.id}&value=${modalInstance.currentApplication.value}&approvedAmount=${modalInstance.form.approvedAmount}`;

            // Create Claim and handle success / fail responses.
            $http.put(createUnderwritingEvaluationEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    demoApp.getApplicationStates();
                    demoApp.getUnderwritingStates();
                },
                (result) => {
                    modalInstance.displayMessage(result);
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    // Close create Claim modal dialogue.
    modalInstance.cancel = () => $uibModalInstance.dismiss();

    // Validate the Claim.
    function invalidFormInput() {
        return isNaN(modalInstance.form.approvedAmount)|| (modalInstance.currentApplication.fname === undefined)||(modalInstance.currentApplication.lname === undefined);
    }
});
//Underwriting Evaluation controller ends

//Company response controller starts here
app.controller('ModalInstanceCompanyResponseCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;
    modalInstance.currentResponse=demoApp.currentResponse;

    // Company Response.
    modalInstance.createCompanyResponse = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();

            const createCompanyResponseEndpoint = `${apiBaseURL}company-response?fname=${modalInstance.currentResponse.fname}&lname=${modalInstance.currentResponse.lname}&approvedAmount=${modalInstance.currentResponse.approvedAmount}&insuranceStatus=${modalInstance.currentResponse.insuranceStatus}&insuranceID=${modalInstance.currentResponse.insuranceID}`;

            // Create Claim and handle success / fail responses.
            $http.put(createCompanyResponseEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    demoApp.getApplicationStates();
                    demoApp.getUnderwritingStates();
                },
                (result) => {
                    modalInstance.displayMessage(result);
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    // Close create Claim modal dialogue.
    modalInstance.cancel = () => $uibModalInstance.dismiss();

    // Validate the Claim.
    function invalidFormInput() {
        return (modalInstance.currentResponse.fname === undefined)||(modalInstance.currentResponse.lname === undefined);
    }
});
//Company response controller ends

// Controller for success/fail modal dialogue.
app.controller('messageCtrl', function ($uibModalInstance, message) {
    const modalInstanceTwo = this;
    modalInstanceTwo.message = message.data;
});
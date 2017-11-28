/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
$(document).ready(function(){
	getProductTestPlans();
    $(document.body).on('click', '.expand' ,function(){
        if($(this).closest("tr").next().hasClass("hidden")){
            $(this).closest("tr").next().removeClass("hidden");
            $(this).closest("tr").next().addClass("visible");
            $(this).html("-");
        } else {
            $(this).closest("tr").next().removeClass("visible");
            $(this).closest("tr").next().addClass("hidden");
            $(this).html("+");
        }
    });

    $(document.body).on('click', '.expand-test-plans' ,function(){
        var id = $(this).closest("tr").attr("id");
        $(this).closest("tr").addClass("selected");
        getTestPlans(id);
        $(this).closest("tr").removeClass("selected");

        $(this).closest("tr").next().removeClass("hidden");
        $(this).closest("tr").next().addClass("visible");
        $(this).html("-");
        $(this).removeClass("expand-test-plans");
        $(this).addClass("collapse-test-plans");
    });

    $(document.body).on('click', '.collapse-test-plans' ,function(){
        $(this).closest("tr").next().removeClass("visible");
        $(this).closest("tr").next().addClass("hidden");
        $(this).html("+");
        $(this).removeClass("collapse-test-plans");
        $(this).addClass("expand-test-plans");
    });

    $(document.body).on('click', '.expand-test-scenarios' ,function(){
        var id = $(this).closest("tr").attr("id");
        $(this).closest("tr").addClass("selected");
        getTestScenarios(id);
        $(this).closest("tr").removeClass("selected");

        $(this).closest("tr").next().removeClass("hidden");
        $(this).closest("tr").next().addClass("visible");
        $(this).html("-");
        $(this).removeClass("expand-test-scenarios");
        $(this).addClass("collapse-test-scenarios");
    });

    $(document.body).on('click', '.collapse-test-scenarios' ,function(){
        $(this).closest("tr").next().removeClass("visible");
        $(this).closest("tr").next().addClass("hidden");
        $(this).html("+");
        $(this).removeClass("collapse-test-scenarios");
        $(this).addClass("expand-test-scenarios");
    });

    $(document.body).on('click', '.expand-test-cases' ,function(){
        var id = $(this).closest("tr").attr("id");
        $(this).closest("tr").addClass("selected");
        getTestCases(id);
        $(this).closest("tr").removeClass("selected");

        $(this).closest("tr").next().removeClass("hidden");
        $(this).closest("tr").next().addClass("visible");
        $(this).html("-");
        $(this).removeClass("expand-test-cases");
        $(this).addClass("collapse-test-cases");
    });

    $(document.body).on('click', '.collapse-test-cases' ,function(){
        $(this).closest("tr").next().removeClass("visible");
        $(this).closest("tr").next().addClass("hidden");
        $(this).html("+");
        $(this).removeClass("collapse-test-cases");
        $(this).addClass("expand-test-cases");
    });

});

/**
 * Retrieves all product test plans by calling the product test plan Rest API
 */
function getProductTestPlans () {
     $.ajax({
         type: "GET",
         url: "api/product-test-plans",
         dataType: "json",
         success: function (data) {

         	for (var i = 0; i < data.length; i++) {
         		$("#product-test-plans > tbody").append("<tr id = " + data[i].id + "><td><button class='expand-test-plans'>+</button>&nbsp;" + data[i].id + "</td><td>"
         			+ data[i].startTimestamp + "</td><td>"
         			+ data[i].endTimestamp + "</td><td>"
         			+ data[i].status + "</td><td>"
         			+ data[i].product_name + "</td><td>"
         			+ data[i].product_version + "</td></tr>");
         	}      	
         },

         error: function (status) {
             console.log(status);
         }
    });
}

/**
 * Retrieves all test plans of product test plan by calling the test plan Rest API
 *
 * @param id product test plan id
 */
function getTestPlans (id) {
     $.ajax({
         type: "GET",
         url: "api/test-plans?product-test-plan=" + id,
         dataType: "json",
         async : false,
         success: function (data) {
            var innerHtml = "<tr class='hidden'><td colspan=6><h3>Test Plans</h3><hr/>"
                    + "<table class='table test-plans'><thead><tr>"
                    + "<th>ID</th><th>Name</th><th>Deployment Pattern</th>" 
                    + "<th>Start Time</th><th>End Time</th><th>Status</th></tr></thead><tbody>";
         	for (var i = 0; i < data.length; i++) {
                innerHtml += "<tr id = " + data[i].id + "><td><button class='expand-test-scenarios'>+</button>&nbsp;"
                    + data[i].id + "</td><td>"
                    + data[i].name + "</td><td>"
                    + data[i].deploymentPattern + "</td><td>"
                    + data[i].startTimestamp + "</td><td>"
                    + data[i].modifiedTimestamp + "</td><td>"
                    + data[i].status + "</td></tr>";
         	} 
            innerHtml += "</tbody></table></td></tr>"; 
            $("#product-test-plans > tbody > tr.selected").after(innerHtml);
         	$("#product-test-plans > tbody > tr th").addClass("col-md-1");    
                	
         },

         error: function (status) {
             console.log(status);
         }
    });
}

/**
 * Retrieves all test scenarios of test plan by calling the test scenarios Rest API
 *
 * @param id test scenario id
 */
function getTestScenarios (id) {
     $.ajax({
         type: "GET",
         url: "api/test-scenarios?test-plan-id=" + id,
         dataType: "json",
         async : false,
         success: function (data) {
            var innerHtml = "<tr class='test-scenario hidden'><td colspan=6><h4>Test Scenarios</h4><hr/>"
                    + "<table class='table test-scenarios'><thead><tr>"
                    + "<th>ID</th><th>Name</th><th>Status</th></tr></thead><tbody>";
            for (var i = 0; i < data.length; i++) {
                innerHtml += "<tr class = " + data[i].id + "><td><button class='expand-test-cases'>+</button>&nbsp;"
                    + data[i].id + "</td><td>"
                    + data[i].name + "</td><td>"
                    + data[i].status + "</td></tr>";
            } 
            innerHtml += "</tbody></table></td></tr>"; 
            $(".test-plans > tbody tr.selected").after(innerHtml);
            $(".test-plans > tbody tr th").addClass("col-md-1");          
         },

         error: function (status) {
             console.log(status);
         }
    });
}

/**
 * Retrieves all test cases of test scenario by calling the test cases Rest API
 *
 * @param id product test cases id
 */
function getTestCases (id) {
     $.ajax({
         type: "GET",
         url: "api/test-cases?test-scenario-id=" + id,
         dataType: "json",
         async : false,
         success: function (data) {
            var innerHtml = "<tr class='test-case hidden'><td colspan=6><h5><strong>Test Cases<strong></h5><hr/>"
                    + "<table class='table test-cases'><thead><tr>"
                    + "<th>ID</th><th>Name</th><th>Status</th></tr></thead><tbody>";
            for (var i = 0; i < data.length; i++) {
                innerHtml += "<tr class = " + data[i].id + "><td>"
                    + data[i].id + "</td><td>"
                    + data[i].name + "</td><td>"
                    + data[i].status + "</td></tr>";
            } 
            innerHtml += "</tbody></table></td></tr>"; 
            $(".test-scenarios > tbody tr.selected").after(innerHtml);
            $(".test-scenarios > tbody tr th").addClass("col-md-1");          
         },

         error: function (status) {
             console.log(status);
         }
    });
}

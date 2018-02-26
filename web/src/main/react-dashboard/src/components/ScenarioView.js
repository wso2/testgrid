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

import React, { Component } from 'react';
import '../App.css';
import {
    Table,
    TableBody,
    TableHeader,
    TableHeaderColumn,
    TableRow,
    TableRowColumn,
} from 'material-ui/Table';
import Subheader from 'material-ui/Subheader';
import SingleRecord from './SingleRecord.js';
import {add_current_scenario} from '../actions/testGridActions.js';
import {HTTP_UNAUTHORIZED, LOGIN_URI} from '../constants.js';

class ScenarioView extends Component {

    constructor(props) {
        super(props);
        this.state = {
            hits:{
                testScenarios:[]
            }
        }
    }

    nevigateToRoute(route, scenario) {
        this.props.dispatch(add_current_scenario(scenario));
        this.props.history.push(route);
    }

    handleError(response) {
        if (response.status.toString() === HTTP_UNAUTHORIZED) {
            window.location.replace(LOGIN_URI);
            return response;
        }
    }

    componentDidMount() {
        var url = "/testgrid/v0.9/api/test-plans/"+this.props.active.reducer.currentInfra.infrastructureId+"?require-test-scenario-info=true";

        fetch(url, {
            method: "GET",
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json'
            }
        })
        .then(this.handleError)
        .then(response => {
            return response.json()
        })
            .then(data => this.setState({ hits: data }));
    }

    render() {
        console.log(this.props);
        var infraString = "";
        return (<div>
            <Subheader style={{ fontSize: '20px' }} >  <i> {this.props.active.reducer.currentProduct.productName} {this.props.active.reducer.currentProduct.productVersion}
                {this.props.active.reducer.currentProduct.productChannel} /  {this.props.active.reducer.currentDeployment.deploymentName} / {this.props.active.reducer.currentInfra.deploymentName}</i> </Subheader>
            <Table>
                <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
                    <TableRow>
                        <TableHeaderColumn><h2>Scenario ID</h2></TableHeaderColumn>
                        <TableHeaderColumn><h2>Scenario Description</h2></TableHeaderColumn>
                        <TableHeaderColumn><h2>Scenario Status</h2></TableHeaderColumn>
                    </TableRow>
                </TableHeader>
                <TableBody displayRowCheckbox={false}>
                    {this.state.hits.testScenarios.map((data, index) => {
                        return (<TableRow key={index}>
                            <TableRowColumn>
                                <h4>{data.name}</h4></TableRowColumn>
                            <TableRowColumn>
                                <h4>{data.description}</h4></TableRowColumn>
                            <TableRowColumn>
                                <SingleRecord value={data.status}
                                    nevigate={() => this.nevigateToRoute("/testgrid/v0.9/testcases/scenario/" + data.id, {
                                        scenarioId: data.id,
                                        scenarioName: data.name,
                                    })} />
                            </TableRowColumn>
                        </TableRow>)
                    })}
                </TableBody>
            </Table>
        </div>);
    }
}

export default ScenarioView;
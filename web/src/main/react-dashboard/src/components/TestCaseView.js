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


class TestCaseView extends Component {

    constructor(props) {
        super(props);

        this.state = {
            hits: {
                testCases: []
            }

        }

    }

    componentDidMount() {
        var url = '/testgrid/v0.9/api/test-scenarios/'+this.props.active.reducer.currentScenario.scenarioId;

        fetch(url, {
            method: "GET",
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json'
            }
        }).then(response => {
            return response.json();
        })
            .then(data => this.setState({ hits: data }));

    }


    render() {
        return (
            <div>
                <Subheader style={{ fontSize: '20px' }} > <i>{this.props.active.reducer.currentProduct.productName} {this.props.active.reducer.currentProduct.productVersion} {this.props.active.reducer.currentProduct.productChannel} / {this.props.active.reducer.currentDeployment.deploymentName} /{this.props.active.reducer.currentInfra.deploymentName} /
                {this.props.active.reducer.currentScenario.scenarioName} </i>
                </Subheader>

                <Table>
                    <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
                        <TableRow>
                            <TableHeaderColumn><h2>TestCase</h2></TableHeaderColumn>
                            <TableHeaderColumn><h2>TestResult</h2></TableHeaderColumn>
                            <TableHeaderColumn><h2>Error message</h2></TableHeaderColumn>
                        </TableRow>
                    </TableHeader>
                    <TableBody displayRowCheckbox={false}>

                        {this.state.hits.testCases.map((data, index) => {
                            return (<TableRow key={index}>
                                <TableRowColumn>
                                    <h4>{data.name}</h4></TableRowColumn>
                                <TableRowColumn><SingleRecord value={data.success} /></TableRowColumn>
                                <TableRowColumn style={{
                                    color: 'red',
                                    whiteSpace: 'normal',
                                    wordWrap: 'break-word'
                                }}>
                                    <h4>{data.errorMsg}</h4></TableRowColumn>
                            </TableRow>)
                        })}
                    </TableBody>
                </Table>
            </div>
        );
    }
}

export default TestCaseView;
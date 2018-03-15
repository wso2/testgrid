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
import {add_current_infra, add_current_deployment} from '../actions/testGridActions.js';
import FlatButton from 'material-ui/FlatButton';
import Divider from 'material-ui/Divider';
import Moment from 'moment';
import {FAIL, SUCCESS, ERROR, PENDING, RUNNING, HTTP_UNAUTHORIZED, LOGIN_URI, TESTGRID_CONTEXT} from '../constants.js';

class InfraCombinationView extends Component {

    constructor(props) {
        super(props);
        this.state = {
            hits: []
        }
    }

    handleError(response) {
        if (response.status.toString() === HTTP_UNAUTHORIZED) {
            window.location.replace(LOGIN_URI);
        } else if(!response.ok){
            throw Error(response.statusText)
        }
        return response;
    }

    navigateToRoute(route, deployment, testPlan) {
        this.props.dispatch(add_current_deployment(deployment));
        this.props.dispatch(add_current_infra(testPlan));
        this.props.history.push(route);
    }

    componentDidMount() {
        var url = TESTGRID_CONTEXT + "/api/test-plans/history/" + this.props.active.reducer.currentInfra.testPlanId;
        fetch(url, {
            method: "GET",
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json'
            }
        })
        .then(this.handleError)
        .then(response => { return response.json()} )
        .then(data => this.setState({ hits: data }))
        .catch(error => console.error(error));
    }
    render() {
        return (
            <div>
                {(() => {
                    switch (this.props.active.reducer.currentProduct.productStatus) {
                        case FAIL:
                            return <Subheader style={{
                                fontSize: '20px',
                                backgroundColor: "#ffd6d3"
                            }}>
                                <table>
                                    <tbody>
                                        <tr>
                                            <td style={{ padding: 5 }}>
                                                <img
                                                    src={require('../close.png')}
                                                    style={{
                                                        verticalAlign: "middle",
                                                        height: "50px",
                                                        width: "50px"
                                                    }} />
                                            </td>
                                            <i>{this.props.active.reducer.currentProduct.productName}  /  
                                                {this.props.active.reducer.currentDeployment.deploymentPatternName}  <br />
                                                {this.props.active.reducer.currentInfra.infraParameters}
                                            </i>
                                        </tr>
                                    </tbody>
                                </table>
                            </Subheader>;
                        case SUCCESS:
                            return <Subheader style={{
                                fontSize: '20px',
                                backgroundColor: "#cdffba"
                            }}>
                                <table>
                                    <tbody>
                                        <tr>
                                            <td style={{ padding: 5 }}>
                                                <img
                                                    src={require('../success.png')}
                                                    style={{
                                                        verticalAlign: "middle",
                                                        height: "50px",
                                                        width: "50px"
                                                    }} />
                                            </td>
                                            <i>{this.props.active.reducer.currentProduct.productName}  /  
                                                {this.props.active.reducer.currentDeployment.deploymentPatternName}  <br />
                                                {this.props.active.reducer.currentInfra.infraParameters}
                                            </i>
                                        </tr>
                                    </tbody>
                                </table>
                            </Subheader>;
                        case PENDING:
                            return <Subheader style={{
                                fontSize: '20px',
                                backgroundColor: "#cdffba"
                            }}>
                                <table>
                                    <tbody>
                                    <tr>
                                        <td style={{ padding: 5 }}>
                                            <img
                                                src={require('../new.png')}
                                                style={{
                                                    verticalAlign: "middle",
                                                    height: "50px",
                                                    width: "50px"
                                                }} />
                                        </td>
                                        <i>{this.props.active.reducer.currentProduct.productName}  /
                                            {this.props.active.reducer.currentDeployment.deploymentPatternName}  <br />
                                            {this.props.active.reducer.currentInfra.infraParameters}
                                        </i>
                                    </tr>
                                    </tbody>
                                </table>
                            </Subheader>;
                        case RUNNING:
                        default:
                            return <Subheader style={{
                                fontSize: '20px',
                                backgroundColor: "#FFCC80"
                            }}>
                                <table>
                                    <tbody>
                                        <tr>
                                            <td style={{ padding: 5 }}>
                                                <img
                                                    src={require('../wait.gif')}
                                                    style={{
                                                        verticalAlign: "middle",
                                                        height: "50px",
                                                        width: "50px"
                                                    }} />
                                            </td>
                                            <i>{this.props.active.reducer.currentProduct.productName}  / 
                                                 {this.props.active.reducer.currentDeployment.deploymentPatternName}  <br />
                                                {this.props.active.reducer.currentInfra.infraParameters}
                                            </i>
                                        </tr>
                                    </tbody>
                                </table>
                            </Subheader>;
                    }
                })()}
                <Divider />
                <Table fixedHeader={false} style={{ tableLayout: 'auto' }}>
                    <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
                        <TableRow>
                            <TableHeaderColumn><h2>#</h2></TableHeaderColumn>
                            <TableHeaderColumn><h2>Status</h2></TableHeaderColumn>
                            <TableHeaderColumn><h2>Date</h2></TableHeaderColumn>
                            <TableHeaderColumn><h2>Duration</h2></TableHeaderColumn>
                        </TableRow>
                    </TableHeader>
                    <TableBody displayRowCheckbox={false}>

                        {this.state.hits
                            .sort((a, b) => b.createdTimestamp - a.createdTimestamp)
                            .map((data, index) => {
                                return (<TableRow key={index}>
                                    <TableRowColumn>{index + 1}</TableRowColumn>
                                    <TableRowColumn>
                                        <FlatButton style={{ color: '#0E457C' }}
                                            onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/testplans/" + data.id, {
                                                deploymentPatternName: this.props.active.reducer.currentDeployment.deploymentPatternName
                                            }, {
                                                    testPlanId: data.id,
                                                    infraParameters: data.infraParams,
                                                    testPlanStatus: data.status
                                                })}>
                                            <SingleRecord value={data.status} />
                                        </FlatButton>
                                    </TableRowColumn>
                                    <TableRowColumn>{Moment(data.createdTimestamp).calendar()}</TableRowColumn>
                                    <TableRowColumn>
                                        {(() => {
                                            var start = Moment(data.createdTimestamp);
                                            var end = Moment(data.modifiedTimestamp);
                                            var min = end.diff(start, 'minutes');
                                            var hours = Math.floor(min / 60);
                                            var minutes = min % 60;
                                            if (hours > 0) {
                                                return (
                                                    <p> <b> {hours} </b>  Hours <b> {minutes} </b> Minutes </p>
                                                )
                                            } else {
                                                return (
                                                    <p><b> {minutes} </b> Minutes </p>
                                                )
                                            }
                                        })()}
                                    </TableRowColumn>
                                </TableRow>)
                            })}
                    </TableBody>
                </Table>
            </div>
        );
    }
}

export default InfraCombinationView;

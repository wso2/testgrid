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

import React, {Component} from 'react';
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
import {add_current_infra} from '../actions/testGridActions.js';
import FlatButton from 'material-ui/FlatButton';


class InfraCombinationView extends Component {

    constructor(props) {
        super(props);
        this.baseURL = "/testgrid/dashboard" ;
        this.state = {
            hits: []
        }
    }

    nevigateToRoute(route, infra) {
        this.props.dispatch(add_current_infra(infra));
        this.props.history.push(route);
    }

    componentDidMount() {
        var url = this.baseURL + "/api/test-plans?deployment-pattern-id=" + this.props.active.reducer.currentDeployment.deploymentId + "&date=" + this.props.active.reducer.currentProduct.productDate + "&require-test-scenario-info=false";

        fetch(url, {
            method: "GET",
            headers: {
                'Accept': 'application/json'
            }
        }).then(response => {
            return response.json()
        })
            .then(data => this.setState({hits: data}));
    }

    render() {
        return (
            <div>
                <Subheader style={{fontSize: '20px'}}>
                    <i>{this.props.active.reducer.currentProduct.productName} {this.props.active.reducer.currentProduct.productVersion} {this.props.active.reducer.currentProduct.productChannel} / {this.props.active.reducer.currentDeployment.deploymentName} </i>
                </Subheader>
                <Table>
                    <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
                        <TableRow>
                            <TableHeaderColumn><h2>Infrastructure</h2></TableHeaderColumn>
                            <TableHeaderColumn><h2>Status</h2></TableHeaderColumn>
                            <TableHeaderColumn/>
                        </TableRow>
                    </TableHeader>
                    <TableBody displayRowCheckbox={false}>

                        {this.state.hits.map((data, index) => {


                            return (<TableRow key={index}>
                                <TableRowColumn>{data.infraParams}</TableRowColumn>
                                <TableRowColumn><SingleRecord value={data.status}
                                                              nevigate={() => this.nevigateToRoute("/testgrid/v0.9/scenarios/infrastructure/" + data.id, {
                                                                  infrastructureId: data.id,
                                                                  deploymentName: data.infraParams
                                                              })}
                                /></TableRowColumn>
                                <TableRowColumn>
                                    <FlatButton style={{color: '#0E457C'}}
                                                onClick={() => this.nevigateToRoute("/testgrid/v0.9/testplans/" + data.id, {
                                                    testPlanId: data.id,
                                                    infraParameters: data.infraParams,
                                                    testPlanStatus: data.status
                                                })}>
                                        Artifacts
                                    </FlatButton>
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

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
import { add_current_deployment } from '../actions/testGridActions.js';
import Moment from 'moment'

class DeploymentPatternView extends Component {

  constructor(props) {
    super(props)
    this.state = {
      hits: []
    };
  }

  componentDidMount() {
    var url = '/testgrid/v0.9/api/test-plans/product/' + this.props.active.reducer.currentProduct.productId;
    fetch(url, {
      mode: 'cors',
      method: "GET",
      headers: {
        'Accept': 'application/json'
      }
    }).then(response => {
      console.log(response);
      if (response.ok) {
        return response.json();
      }
    })
      .then(data => this.setState({ hits: data }));
  }

  nevigateToRoute(route, deployment) {
    this.props.dispatch(add_current_deployment(deployment));
    this.props.history.push(route);
  }


  render() {
    console.log(this.state);
    var x = {};
    this.state.hits.map((value, index) => {
      if (x[value.lastBuild.deploymentPattern] == undefined) {
        x[value.lastBuild.deploymentPattern] = [{ 'lastBuild': value.lastBuild, 'lastFailed': value.lastFailure }]
      } else {
        x[value.lastBuild.deploymentPattern].push({ 'lastBuild': value.lastBuild, 'lastFailed': value.lastFailure })
      }
    })
    return (
      <div>
        <Subheader style={{ fontSize: '20px' }} > <i> {this.props.active.reducer.currentProduct.productName + " "} </i> </Subheader>
        <Table fixedHeader={false} style={{ tableLayout: 'auto' }}>
          <TableHeader displaySelectAll={false} adjustForCheckbox={false} >
            <TableRow>
              <TableHeaderColumn ><h2>Deployment Pattern</h2></TableHeaderColumn>
              <TableHeaderColumn><h2>Infra Combination</h2></TableHeaderColumn>
              <TableHeaderColumn><h2>Last Build</h2></TableHeaderColumn>
              <TableHeaderColumn><h2>Last Failure</h2></TableHeaderColumn>
              <TableHeaderColumn><h2>Execute</h2></TableHeaderColumn>
            </TableRow>
          </TableHeader>
          <TableBody displayRowCheckbox={false}>
            {Object.keys(x).map((key) => {
              return (
                x[key].map((value, index) => {
                  if (index == 0) {
                    return (
                      <TableRow>
                        <TableRowColumn rowSpan={x[key].length}>{key}</TableRowColumn>
                        <TableRowColumn style={{ whiteSpace: 'normal', wordWrap: 'break-word' }}>{value.lastBuild.infraParams}</TableRowColumn>
                        <TableRowColumn ><SingleRecord value={value.lastBuild.status}
                          nevigate={() => this.nevigateToRoute("/testgrid/v0.9/", {
                            testPlanId: value.lastBuild.id,
                            testPlanInfra: value.lastBuild.infraParams,
                          })} time={Moment(value.lastBuild.modifiedTimestamp).fromNow()}
                        /> </TableRowColumn>
                        <TableRowColumn ><SingleRecord value={value.lastFailed.status}
                          nevigate={() => this.nevigateToRoute("/testgrid/v0.9/", {
                            testPlanId: value.lastFailed.id,
                            testPlanInfra: value.lastFailed.infraParams,
                          })} time={Moment(value.lastFailed.modifiedTimestamp).fromNow()}
                        /> </TableRowColumn>
                        <TableRowColumn> <img src={require('../play.png')} width="48" height="48" /></TableRowColumn>
                      </TableRow>

                    )
                  } else {
                    return (

                      <TableRow>
                        <TableRowColumn >{value.lastBuild.infraParams}</TableRowColumn>
                        <TableRowColumn ><SingleRecord value={value.lastBuild.status}
                          nevigate={() => this.nevigateToRoute("/testgrid/v0.9/", {
                            testPlanId: value.lastBuild.id,
                            testPlanInfra: value.lastBuild.infraParams,
                          })} time={Moment(value.lastBuild.modifiedTimestamp).fromNow()}
                        /> </TableRowColumn>
                        <TableRowColumn ><SingleRecord value={value.lastFailed.status}
                          nevigate={() => this.nevigateToRoute("/testgrid/v0.9/", {
                            testPlanId: value.lastFailed.id,
                            testPlanInfra: value.lastFailed.infraParams,
                          })} time={Moment(value.lastFailed.modifiedTimestamp).fromNow()}
                        /> </TableRowColumn>
                        <TableRowColumn> <img src={require('../play.png')} width="48" height="48" /></TableRowColumn>
                      </TableRow>
                    )
                  }
                })
              )
            })}
          </TableBody>
        </Table>
      </div>
    );
  }
}

export default DeploymentPatternView;
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
import { add_current_deployment, add_current_infra } from '../actions/testGridActions.js';
import Moment from 'moment'
import ReactTooltip from 'react-tooltip'
import FlatButton from 'material-ui/FlatButton';
import {FAIL,SUCCESS,ERROR,PENDING,RUNNING } from '../constants.js';

class DeploymentPatternView extends Component {

  constructor(props) {
    super(props)
    this.baseURL = "/testgrid/dashboard";
    this.state = {
      hits: []
    };
  }

  handleError(response) {
    if (!response.ok) {
      throw Error(response.statusText)
    }
    return response;
  }

  componentDidMount() {
    var url = this.baseURL + '/api/test-plans/product/' + this.props.active.reducer.currentProduct.productId;
    fetch(url, {
      method: "GET",
      credentials: 'same-origin',
      headers: {
        'Accept': 'application/json'
      }
    })
    .then(this.handleError)
    .then(response => { return response.json()})
    .then(data => this.setState({ hits: data }))
    .catch(error => console.error(error));
  }

  navigateToRoute(route, deployment, testPlan) {
    this.props.dispatch(add_current_deployment(deployment));
    this.props.dispatch(add_current_infra(testPlan));
    this.props.history.push(route);
  }

  render() {
    const FAIL = "FAIL";
    const ERROR = "ERROR";
    const SUCCESS = "SUCCESS";
    const RUNNING = "RUNNING";
    const PENDING = "PENDING";

    var groupByDeployment = {};
    this.state.hits.map((value, index) => {
      if (groupByDeployment[value.lastBuild.deploymentPattern] == undefined) {
        groupByDeployment[value.lastBuild.deploymentPattern] = [{ 'lastBuild': value.lastBuild, 'lastFailed': value.lastFailure }]
      } else {
        groupByDeployment[value.lastBuild.deploymentPattern].push({ 'lastBuild': value.lastBuild, 'lastFailed': value.lastFailure })
      }
    })
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
                      <i> {this.props.active.reducer.currentProduct.productName + " "} </i>
                    </tr>
                  </tbody>
                </table>
              </Subheader>;
            case SUCCESS :
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
                      <i> {this.props.active.reducer.currentProduct.productName + " "} </i>
                    </tr>
                  </tbody>
                </table>
              </Subheader>;
            case PENDING:
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
                      <i> {this.props.active.reducer.currentProduct.productName + " "} </i>
                    </tr>
                  </tbody>
                </table>
              </Subheader>;
          }
        })()}
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
            {Object.keys(groupByDeployment).map((key) => {
              return (
                groupByDeployment[key].map((value, index) => {
                  if (index == 0) {
                    return (
                      <TableRow>
                        <TableRowColumn rowSpan={groupByDeployment[key].length}>{key}</TableRowColumn>
                        <TableRowColumn style={{ whiteSpace: 'normal', wordWrap: 'break-word' }}>
                          <FlatButton style={{ color: '#0E457C' }} data-tip="View History"
                            onClick={() => this.navigateToRoute(this.baseURL + "/testplans/history/"
                              + value.lastBuild.id, {
                                deploymentPatternName: key
                              }, {
                                testPlanId: value.lastBuild.id,
                                infraParameters: value.lastBuild.infraParams,
                                testPlanStatus: value.lastBuild.status
                              })}>
                            {value.lastBuild.infraParams}
                          </FlatButton>
                          <ReactTooltip />
                        </TableRowColumn>
                        <TableRowColumn>
                          <FlatButton style={{ color: '#0E457C' }}
                            onClick={() => this.navigateToRoute(this.baseURL + "/testplans/" + value.lastBuild.id, {
                              deploymentPatternName: key
                            }, {
                                testPlanId: value.lastBuild.id,
                                infraParameters: value.lastBuild.infraParams,
                                testPlanStatus: value.lastBuild.status
                              })}>
                            <SingleRecord value={value.lastBuild.status}
                              time={value.lastBuild.modifiedTimestamp} />
                          </FlatButton>
                        </TableRowColumn>
                        <TableRowColumn>
                          {(() => {
                            if (value.lastFailed.modifiedTimestamp) {
                              return (
                                <FlatButton style={{ color: '#0E457C' }}
                                  onClick={() => this.navigateToRoute(this.baseURL + "/testplans/"
                                    + value.lastFailed.id, { deploymentPatternName: key },
                                    {
                                      testPlanId: value.lastFailed.id,
                                      infraParameters: value.lastFailed.infraParams,
                                      testPlanStatus: value.lastFailed.status
                                    }
                                  )}>
                                  <SingleRecord value={value.lastFailed.status}
                                    time={value.lastFailed.modifiedTimestamp}
                                  />
                                </FlatButton>
                              );
                            } else {
                              return (
                                <FlatButton>No failed builds yet!</FlatButton>
                              )
                            }
                          })()}
                        </TableRowColumn>
                        <TableRowColumn> <img src={require('../play.png')} width="48" height="48" data-tip="Execute Job"
                          onClick={() => { window.location = '/job/'+ this.props.active.reducer.currentProduct.productName +'/build' }} /><ReactTooltip /></TableRowColumn>
                      </TableRow>
                    )
                  } else {
                    return (
                      <TableRow>
                        <TableRowColumn style={{ whiteSpace: 'normal', wordWrap: 'break-word' }}>
                          <FlatButton style={{ color: '#0E457C' }} data-tip="View History"
                            onClick={() => this.navigateToRoute(this.baseURL + "/testplans/history/" + value.lastBuild.id, {
                              deploymentPatternName: key
                            }, {
                                testPlanId: value.lastBuild.id,
                                infraParameters: value.lastBuild.infraParams,
                                testPlanStatus: value.lastBuild.status
                              })}>
                            {value.lastBuild.infraParams}
                          </FlatButton>
                          <ReactTooltip />
                        </TableRowColumn>
                        <TableRowColumn>
                          <FlatButton style={{ color: '#0E457C' }}
                            onClick={() => this.navigateToRoute(this.baseURL + "/testplans/" + value.lastBuild.id, {
                              deploymentPatternName: key
                            }, {
                                testPlanId: value.lastBuild.id,
                                infraParameters: value.lastBuild.infraParams,
                                testPlanStatus: value.lastBuild.status
                              })}>
                            <SingleRecord value={value.lastBuild.status}
                              time={value.lastBuild.modifiedTimestamp} />
                          </FlatButton>
                        </TableRowColumn>
                        <TableRowColumn>
                          {(() => {
                            if (value.lastFailed.modifiedTimestamp) {
                              return (
                                <FlatButton style={{ color: '#0E457C' }}
                                  onClick={() => this.navigateToRoute(this.baseURL + "/testplans/" + value.lastFailed.id,
                                    { deploymentPatternName: key },
                                    {
                                      testPlanId: value.lastFailed.id,
                                      infraParameters: value.lastFailed.infraParams,
                                      testPlanStatus: value.lastFailed.status
                                    }
                                  )}>
                                  <SingleRecord value={value.lastFailed.status}
                                    time={value.lastFailed.modifiedTimestamp}
                                  />
                                </FlatButton>
                              );
                            } else {
                              return (
                                <FlatButton>No failed builds yet!</FlatButton>
                              )
                            }
                          })()}
                        </TableRowColumn>
                        <TableRowColumn> <img src={require('../play.png')} width="48" height="48" data-tip="Execute Job"
                          onClick={() => { window.location = '/job/'+ this.props.active.reducer.currentProduct.productName +'/build' }} /><ReactTooltip /></TableRowColumn>
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
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
import Subheader from 'material-ui/Subheader';
import SingleRecord from './SingleRecord.js';
import {add_current_deployment, add_current_infra} from '../actions/testGridActions.js';
import ReactTooltip from 'react-tooltip'
import FlatButton from 'material-ui/FlatButton';
import {HTTP_UNAUTHORIZED, LOGIN_URI, TESTGRID_CONTEXT} from '../constants.js';
import {Table} from 'reactstrap';

class DeploymentPatternView extends Component {

  constructor(props) {
    super(props);
    this.state = {
      hits: [],
      product: null
    };
  }

  handleError(response) {
    if (response.status.toString() === HTTP_UNAUTHORIZED) {
      window.location.replace(LOGIN_URI);
    } else if (!response.ok) {
      throw Error(response.statusText)
    }
    return response;
  }

  componentDidMount() {
    let currentProduct = this.props.active.reducer.currentProduct;
    if (!currentProduct) {
      let url = TESTGRID_CONTEXT + '/api/products/product-status/' + window.location.href.split("/").pop();
      fetch(url, {
        method: "GET",
        credentials: 'same-origin',
        headers: {
          'Accept': 'application/json'
        }
      }).then(this.handleError)
        .then(response => {
          return response.json();
        })
        .then(data => {
          currentProduct = data;
          return currentProduct;
        })
        .then(currentProduct => this.getDeploymentDetails(currentProduct))
        .catch(error => console.error(error));
    } else {
      this.getDeploymentDetails(currentProduct);
    }
  }

  getDeploymentDetails(currentProduct) {
    let url = TESTGRID_CONTEXT + '/api/test-plans/product/' + currentProduct.productId;
    fetch(url, {
      method: "GET",
      credentials: 'same-origin',
      headers: {
        'Accept': 'application/json'
      }
    }).then(this.handleError)
      .then(response => {
        return response.json()
      })
      .then(data => this.setState({hits: data, product: currentProduct}))
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
    const INCOMPLETE = "INCOMPLETE";
    const DID_NOT_RUN = "DID_NOT_RUN";

    var groupByDeployment = {};
    this.state.hits.map((value, index) => {
      if (groupByDeployment[value.lastBuild.deploymentPattern] === undefined) {
        groupByDeployment[value.lastBuild.deploymentPattern] = [{
          'lastBuild': value.lastBuild, 'lastFailed':
          value.lastFailure
        }]
      } else {
        groupByDeployment[value.lastBuild.deploymentPattern].push({
          'lastBuild': value.lastBuild, 'lastFailed':
          value.lastFailure
        })
      }
    });
    return (
      <div>
        {this.state && this.state.product && (() => {
          switch (this.state.product.productStatus) {
            case FAIL:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#ffd6d3"
              }}>
                <Table responsive bordered size="sm">
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../close.png')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i> {this.state.product.productName + " "} </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
            case SUCCESS :
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#cdffba"
              }}>
                <Table responsive bordered size="sm">
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../success.png')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i> {this.state.product.productName + " "} </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
            case ERROR :
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#ffd6d3"
              }}>
                <Table responsive bordered size="sm">
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../error.png')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i> {this.state.product.productName + " "} </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
            case INCOMPLETE :
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#cdffba"
              }}>
                <Table responsive bordered size="sm">
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../incomplete.png')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i> {this.state.product.productName + " "} </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
            case DID_NOT_RUN :
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#cdffba"
              }}>
                <Table responsive bordered size="sm">
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../did_not_run.png')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i> {this.state.product.productName + " "} </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
            case PENDING:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#cdffba"
              }}>
                <Table responsive bordered size="sm">
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../new.png')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i> {this.state.product.productName + " "} </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
            case RUNNING:
            default:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#FFCC80"
              }}>
                <Table responsive bordered size="sm">
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../wait.gif')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i> {this.state.product.productName + " "} </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
          }
        })()}
        <Table responsive bordered className='deployment-pattern-view' fixedHeader={false} size="sm">
          <thead displaySelectAll={false} adjustForCheckbox={false}>
          <tr style={{borderBottom: '0'}}>
            <th rowSpan='2' className="text-center">Deployment Pattern</th>
            <th className="text-center">Infra Combination</th>
            <th rowSpan='2' className="text-center">Last Build</th>
            <th rowSpan='2' className="text-center">Last Failure</th>
            <th rowSpan='2' className="text-center">Execute</th>
          </tr>
          <tr class='infra-param-header'>
            <td>
              <p className="text-center">OS</p>
              <p className="text-center">Database</p>
              <p className="text-center">JDK</p>
            </td>
          </tr>
          </thead>
          <tbody displayRowCheckbox={false}>
          {this.state && this.state.product && Object.keys(groupByDeployment).map((key) => {
            return (
              groupByDeployment[key].map((value, index) => {
                var infraParameters = JSON.parse(value.lastBuild.infraParams);
                if (index === 0) {
                  return (
                    <tr>
                      <td class='deployment-pattern-name'
                          rowSpan={groupByDeployment[key].length}>{key}</td>
                      <td style={{whiteSpace: 'normal', wordWrap: 'break-word'}}>
                        <FlatButton class='view-history' data-tip="View History"
                                    onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                                      this.state.product.productName + "/" + key + "/" +
                                      value.lastBuild.id +
                                      "/infra", {deploymentPatternName: key}, {
                                      testPlanId: value.lastBuild.id,
                                      infraParameters: value.lastBuild.infraParams,
                                      testPlanStatus: value.lastBuild.status
                                    })}>
                          <p class='infra-param'>
                            {infraParameters.OS} {infraParameters.OSVersion}
                          </p>
                          <p class='infra-param'>
                            {infraParameters.DBEngine} {infraParameters.DBEngineVersion}
                          </p>
                          <p class='infra-param'>
                            {infraParameters.JDK}
                          </p>
                        </FlatButton>
                        <ReactTooltip/>
                      </td>
                      <td>
                        <FlatButton
                          onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                            this.state.product.productName + "/" + key + "/test-plans/" +
                            value.lastBuild.id, {
                            deploymentPatternName: key
                          }, {
                            testPlanId: value.lastBuild.id,
                            infraParameters: value.lastBuild.infraParams,
                            testPlanStatus: value.lastBuild.status
                          })}>
                          <SingleRecord value={value.lastBuild.status}
                                        time={value.lastBuild.modifiedTimestamp}/>
                        </FlatButton>
                      </td>
                      <td>
                        {(() => {
                          if (value.lastFailed.modifiedTimestamp) {
                            return (
                              <FlatButton
                                onClick={() => this.navigateToRoute(TESTGRID_CONTEXT +
                                  "/" + this.state.product.productName + "/" + key +
                                  "/test-plans/" + value.lastFailed.id,
                                  {deploymentPatternName: key}, {
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
                              <FlatButton
                                style={{fontSize: '16px'}}>No failed builds yet!
                              </FlatButton>
                            )
                          }
                        })()}
                      </td>
                      <td><img src={require('../play.png')} width="48" height="48"
                               data-tip="Execute Job" onClick={() => {
                        window.location = '/admin/job/' +
                          this.state.product.productName + '/build'
                      }}/>
                        <ReactTooltip/></td>
                    </tr>
                  )
                } else {
                  return (
                    <tr>
                      <td style={{whiteSpace: 'normal', wordWrap: 'break-word'}}>
                        <FlatButton class='view-history' data-tip="View History"
                                    onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                                      this.state.product.productName + "/" + key + "/" +
                                      value.lastBuild.id + "/infra",
                                      {deploymentPatternName: key}, {
                                        testPlanId: value.lastBuild.id,
                                        infraParameters: value.lastBuild.infraParams,
                                        testPlanStatus: value.lastBuild.status
                                      })}>
                          <p class='infra-param'>
                            {infraParameters.OS} {infraParameters.OSVersion}
                          </p>
                          <p class='infra-param'>
                            {infraParameters.DBEngine} {infraParameters.DBEngineVersion}
                          </p>
                          <p class='infra-param'>
                            {infraParameters.JDK}
                          </p>
                        </FlatButton>
                        <ReactTooltip/>
                      </td>
                      <td>
                        <FlatButton
                          onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                            this.state.product.productName + "/" + key + "/test-plans/" +
                            value.lastFailed.id, {
                            deploymentPatternName: key
                          }, {
                            testPlanId: value.lastBuild.id,
                            infraParameters: value.lastBuild.infraParams,
                            testPlanStatus: value.lastBuild.status
                          })}>
                          <SingleRecord value={value.lastBuild.status}
                                        time={value.lastBuild.modifiedTimestamp}/>
                        </FlatButton>
                      </td>
                      <td>
                        {(() => {
                          if (value.lastFailed.modifiedTimestamp) {
                            return (
                              <FlatButton
                                onClick={() => this.navigateToRoute(TESTGRID_CONTEXT
                                  + "/" + this.state.product.productName + "/" + key +
                                  "/test-plans/" + value.lastFailed.id,
                                  {deploymentPatternName: key},
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
                              <FlatButton style={{fontSize: '16px'}}>
                                No failed builds yet!
                              </FlatButton>
                            )
                          }
                        })()}
                      </td>
                      <td><img src={require('../play.png')} width="48" height="48"
                               data-tip="Execute Job" onClick={() => {
                        window.location =
                          '/admin/job/' + this.state.product.productName + '/build'
                      }}/><ReactTooltip/>
                      </td>
                    </tr>
                  )
                }
              })
            )
          })}
          </tbody>
        </Table>
      </div>
    );
  }
}

export default DeploymentPatternView;
